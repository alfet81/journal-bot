package com.alfet.journalbot.drive;

import module java.base;

import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.FOLDER;
import static com.alfet.journalbot.util.MiscUtils.asOptional;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.alfet.journalbot.gof.cor.AbstractRequestHandler;
import com.alfet.journalbot.google.api.GoogleDriveService;
import com.google.api.services.drive.model.File;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter(PROTECTED)
@RequiredArgsConstructor
abstract class AbstractGoogleDriveFolderLocator extends AbstractRequestHandler<FolderParams> {

  private static final String FOLDER_SEARCH_QUERY =
      "mimeType = '%s' and name = '%s' and trashed = false";

  private static final String PARENT_FOLDER_PARAM = " and '%s' in parents";

  private final GoogleDriveService googleDriveService;

  @Override
  public void handle(FolderParams folderParams) {

    var folderSearchQuery = prepareFolderSearchQuery(folderParams);

    getLogger().atDebug().log("Prepared folder search query: {}", folderSearchQuery);

    var folder = findFolder(folderSearchQuery).orElseGet(() -> createFolder(folderParams));

    getLogger().atDebug().log("Located folder: {}", folder);

    folderParams.setCurrentFolder(folder);

    super.handle(folderParams);
  }

  protected String prepareFolderSearchQuery(FolderParams params) {

    var query = FOLDER_SEARCH_QUERY.formatted(FOLDER.getMimeType(), getFolderName(params));

    var parentFolderParam = asOptional(params.getCurrentFolderId())
        .map(PARENT_FOLDER_PARAM::formatted).orElse(EMPTY);

    return query + parentFolderParam;
  }

  protected Optional<File> findFolder(String folderSearchQuery) {
    getLogger().atDebug().log("Searching folder for query: {}", folderSearchQuery);
    return googleDriveService.getFilesPage(folderSearchQuery).stream().findFirst();
  }

  protected File createFolder(FolderParams folderParams) {
    var folderName = getFolderName(folderParams);
    getLogger().atDebug().log("Creating folder '{}' inside parent folder with id '{}'",
        folderName, folderParams.getCurrentFolder().getId());
    var folder = googleDriveService.createFolder(folderName, folderParams.getCurrentFolder());
    getLogger().atDebug().log("Created folder: {}", folder);
    return folder;
  }

  protected abstract String getFolderName(FolderParams params);

  protected Logger getLogger() {
    return LoggerFactory.getLogger(getClass());
  }
}
