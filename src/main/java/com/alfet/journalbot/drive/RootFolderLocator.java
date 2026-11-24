package com.alfet.journalbot.drive;

import module java.base;

import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.google.api.GoogleDriveService;
import com.google.api.services.drive.model.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("rootFolderLocator")
class RootFolderLocator extends AbstractGoogleDriveFolderLocator {

  static final String ROOT_FOLDER_NAME = "Journal";

  RootFolderLocator(GoogleDriveService googleDriveService) {
    super(googleDriveService);
  }

  @Override
  protected Optional<File> findFolder(String folderSearchQuery) {
    var optionalFile = super.findFolder(folderSearchQuery);
    if (optionalFile.isEmpty()) {
      var message = "Root folder '%s' is missing".formatted(ROOT_FOLDER_NAME);
      throw new ApplicationException(message);
    }
    return optionalFile;
  }

  @Override
  protected String getFolderName(FolderParams folderParams) {
    return ROOT_FOLDER_NAME;
  }
}
