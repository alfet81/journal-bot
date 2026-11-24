package com.alfet.journalbot.drive;

import com.alfet.journalbot.google.api.GoogleDriveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("yearFolderLocator")
class YearFolderLocator extends AbstractGoogleDriveFolderLocator {

  YearFolderLocator(GoogleDriveService googleDriveService) {
    super(googleDriveService);
  }

  @Override
  protected String getFolderName(FolderParams folderParams) {
    return String.valueOf(folderParams.getDate().getYear());
  }
}
