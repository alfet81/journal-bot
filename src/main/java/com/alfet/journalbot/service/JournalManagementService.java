package com.alfet.journalbot.service;

import module java.base;

import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.DOCUMENT;

import com.alfet.journalbot.drive.DocumentFolderLocator;
import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.google.api.GoogleDocsService;
import com.alfet.journalbot.google.api.GoogleDriveService;
import com.alfet.journalbot.html.DetailsDocumentGenerator;
import com.alfet.journalbot.log.domain.LogDocumentParams;
import com.alfet.journalbot.log.domain.LogRecord;
import com.alfet.journalbot.util.MiscUtils;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JournalManagementService {

  private static final String DOCUMENT_SEARCH_QUERY
      = "mimeType = '%s' and name = '%s' and trashed = false";

  private static final String CONTENT_PLACEHOLDER = "{{%s_marker}}";

  private final GoogleDriveService googleDriveService;
  private final GoogleDocsService googleDocsService;
  private final DetailsDocumentGenerator detailsDocumentGenerator;
  private final DocumentFolderLocator documentFolderLocator;

  private static String prepareDocumentSearchQuery(String title) {
    return DOCUMENT_SEARCH_QUERY.formatted(DOCUMENT.getMimeType(), title);
  }

  private static String prepareContentPlaceHolder(LogDocumentParams params) {
    return CONTENT_PLACEHOLDER.formatted(MiscUtils.formatDate(params.date()));
  }

  public void processLogRecord(LogRecord logRecord) {
    var params = LogDocumentParams.from(logRecord);
    logger.atInfo().log("Processing log document params: {}", params);
    getOrCreateDocument(params).ifPresent(doc -> updateDocument(params, doc));
  }

  private Optional<Document> getOrCreateDocument(LogDocumentParams params) {

    String searchQuery = prepareDocumentSearchQuery(params.title());

    logger.atDebug().log("Searching for a file with using query: {}", searchQuery);

    return getFiles(searchQuery).stream()
        .findFirst()
        .map((var file) -> getDocument(file.getId()))
        .orElseGet(() -> createDocument(params));
  }

  private void updateDocument(LogDocumentParams params, Document document) {

    var placeholder = prepareContentPlaceHolder(params);
    var text = params.content() + "\n" + placeholder;

    googleDocsService.mergeDocument(document.getDocumentId(), Map.of(placeholder, text));

    logger.atInfo().log("Successfully updated document: '{}'", params.title());
  }

  private List<File> getFiles(String searchQuery) {
    return googleDriveService.getFilesPage(searchQuery);
  }

  private Optional<Document> getDocument(String id) {
    try {
      var doc = googleDocsService.getDocument(id);
      logger.atInfo().log("Found document with id '{}' and title '{}'", doc.getDocumentId(),
          doc.getTitle());
      return Optional.of(doc);
    } catch (ApplicationException _) {
      logger.atInfo().log("No document found with id '{}'", id);
      return Optional.empty();
    }
  }

  private Optional<Document> createDocument(LogDocumentParams params) {
    try {
      var docContent = generateNewDocumentContent(params.date());
      var folder = locateDocumentFileFolder(params.date());
      var docFile = createDocumentFile(params.title(), docContent, folder);
      logger.atInfo().log("Created file for document with title '{}' and id '{}'", params.title(),
          docFile.getId());
      return getDocument(docFile.getId());
    } catch (ApplicationException e) {
      logger.atError().setCause(e)
          .log("Failed to create file for document with parameters: {}", params);
      return Optional.empty();
    }
  }

  private String generateNewDocumentContent(LocalDate documentDate) {
    return detailsDocumentGenerator.generate(documentDate);
  }

  private File locateDocumentFileFolder(LocalDate documentDate) {
    return documentFolderLocator.locateFolder(documentDate)
        .orElseThrow(() -> new ApplicationException("Failed to find/create document folder tree"));
  }

  private File createDocumentFile(String title, String content, File folder) {
    return googleDriveService.createDocumentFile(title, content, folder);
  }
}
