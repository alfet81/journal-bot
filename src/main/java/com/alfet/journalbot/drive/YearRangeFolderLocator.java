package com.alfet.journalbot.drive;

import static com.alfet.journalbot.util.MiscUtils.get5YearRange;

import com.alfet.journalbot.google.api.GoogleDriveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("yearRangeFolderLocator")
class YearRangeFolderLocator extends AbstractGoogleDriveFolderLocator {

  YearRangeFolderLocator(GoogleDriveService googleDriveService) {
    super(googleDriveService);
  }

  @Override
  protected String getFolderName(FolderParams folderParams) {
    return get5YearRange(folderParams.getDate().getYear());
  }
}