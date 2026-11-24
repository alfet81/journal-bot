package com.alfet.journalbot.drive;

import static org.apache.commons.lang3.StringUtils.capitalize;

import com.alfet.journalbot.google.api.GoogleDriveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("monthFolderLocator")
class MonthFolderLocator extends YearFolderLocator {

  MonthFolderLocator(GoogleDriveService googleDriveService) {
    super(googleDriveService);
  }

  @Override
  protected String getFolderName(FolderParams folderParams) {
    return capitalize(folderParams.getDate().getMonth().name().toLowerCase());
  }
}
