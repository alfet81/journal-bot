package com.alfet.journalbot.drive;

import module java.base;
import static com.alfet.journalbot.util.MiscUtils.asOptional;

import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentFolderLocator {

  @Qualifier("rootFolderLocator")
  private final RootFolderLocator rootFolderLocator;

  @Qualifier("yearRangeFolderLocator")
  private final YearRangeFolderLocator yearRangeFolderLocator;

  @Qualifier("yearFolderLocator")
  private final YearFolderLocator yearFolderLocator;

  @Qualifier("monthFolderLocator")
  private final MonthFolderLocator monthFolderLocator;

  @PostConstruct
  void init() {
    rootFolderLocator.setNext(yearRangeFolderLocator);
    yearRangeFolderLocator.setNext(yearFolderLocator);
    yearFolderLocator.setNext(monthFolderLocator);
  }

  public Optional<File> locateFolder(LocalDate date) {

    var params = FolderParams.builder()
        .date(date)
        .build();

    logger.atDebug().log("Locating document folder for date: {}", date);

    rootFolderLocator.handle(params);

    logger.atDebug().log("Successfully located document folder: {}", params.getCurrentFolderName());

    return asOptional(params.getCurrentFolder());
  }
}
