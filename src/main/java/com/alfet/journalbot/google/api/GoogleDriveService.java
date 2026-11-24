package com.alfet.journalbot.google.api;

import module java.base;
import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.DOCUMENT;
import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.FOLDER;
import static com.alfet.journalbot.util.FileUtils.createTempFile;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.alfet.journalbot.exception.ApplicationException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveService {

  private static final String ID_FIELD = "id";
  private static final int PAGE_SIZE = 10;
  private static final String FIELDS = "nextPageToken, files(id, name)";

  private final Drive googleDriveApi;

  private Files getFilesApi() {
    return googleDriveApi.files();
  }

  public List<File> getFilesPage(@NonNull String searchQuery) {
    String pageToken = null;
    List<File> result = new ArrayList<>();

    do {
      var filesPage = getFilesPage(searchQuery, pageToken);
      var files = filesPage.getFiles();
      if (CollectionUtils.isNotEmpty(files)) {
        result.addAll(files);
      }
      pageToken = filesPage.getNextPageToken();
    } while (pageToken != null);

    return result;
  }

  private FileList getFilesPage(String searchQuery, String pageToken) {
    try {
      return getFilesApi().list().setPageSize(PAGE_SIZE).setPageToken(pageToken).setQ(searchQuery)
          .setFields(FIELDS).execute();
    } catch (IOException e) {
      var message = "Failed to get files page with query '%s' and page token '%s"
          .formatted(searchQuery, pageToken);
      throw new ApplicationException(message, e);
    }
  }

  public File createFolder(String folderName, File parentFolder) {

    var parentFolderIds = (parentFolder != null ? List.of(parentFolder.getId()) : null);

    var fileMetadata = new File()
        .setName(folderName)
        .setMimeType(FOLDER.getMimeType().toString())
        .setParents(parentFolderIds);

    logger.atDebug().log("Creating folder with file metadata: {}", fileMetadata);
    try {
      return getFilesApi().create(fileMetadata).setFields(ID_FIELD).execute();
    } catch (IOException e) {
      var message = "Failed to created folder with name '%s' and parent folder '%s'";
      var parentFolderName = (parentFolder != null ? parentFolder.getName() : "null");
      message = message.formatted(folderName, parentFolderName);
      throw new ApplicationException(message, e);
    }
  }

  public File createDocumentFile(String documentTitle, String documentContentHtml, File folder) {

    if (StringUtils.isEmpty(documentTitle)) {
      throw new IllegalArgumentException("Document title cannot be null or empty");
    }
    if (StringUtils.isEmpty(documentContentHtml)) {
      throw new IllegalArgumentException("Document content cannot be null or empty");
    }
    var docTempFile = createDocTempFile(documentTitle, documentContentHtml);
    var parentFolderIds = (folder != null ? List.of(folder.getId()) : null);

    var fileMetadata = new File()
        .setName(documentTitle)
        .setMimeType(DOCUMENT.getMimeType().toString())
        .setParents(parentFolderIds);

    var fileContent = new FileContent(TEXT_HTML_VALUE, docTempFile);

    try {
      return getFilesApi().create(fileMetadata, fileContent).setFields(ID_FIELD).execute();
    } catch (IOException e) {
      var message = "Failed to created document with name '%s'".formatted(documentTitle);
      throw new ApplicationException(message, e);
    } finally {
      var deleted = docTempFile.delete();
      if (deleted) {
        logger.atDebug().log("Successfully deleted temp file: {}", docTempFile.getAbsoluteFile());
      } else {
        logger.atWarn().log("Failed to delete temp file: {}", docTempFile.getAbsoluteFile());
      }
    }
  }

  private java.io.File createDocTempFile(String documentTitle, String documentContent) {
    try {
      return createTempFile(documentTitle, documentContent.getBytes(UTF_8), null);
    } catch (IOException | IllegalArgumentException e) {
      var message = "Failed to create document temporary file with name '%s'"
          .formatted(documentTitle);
      throw new ApplicationException(message, e);
    }
  }
}
