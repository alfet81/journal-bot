package com.alfet.journalbot.drive;

import module java.base;

import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.FOLDER;
import static com.alfet.journalbot.util.MiscUtils.get5YearRange;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static java.util.Collections.emptyList;

import com.alfet.journalbot.google.api.GoogleDriveService;
import com.google.api.services.drive.model.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YearRangeFolderLocatorTest {

  private static final File PARENT_FOLDER = new File()
      .setId(UUID.randomUUID().toString())
      .setName("Parent Folder");

  @Mock
  private GoogleDriveService googleDriveService;

  @InjectMocks
  private YearRangeFolderLocator yearRangeFolderLocator;


  @Test
  void testHandle_WhenFolderExists_ThenSwitchToLocatedFolder() {
    // Given
    var today = LocalDate.now();
    var folderName = get5YearRange(today.getYear());

    var folderParams = FolderParams.builder()
        .date(today)
        .currentFolder(PARENT_FOLDER)
        .build();

    var expectedQuery = "mimeType = '%s' and name = '%s' and trashed = false and '%s' in parents"
        .formatted(FOLDER.getMimeType(), folderName, PARENT_FOLDER.getId());

    var existingFolder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(folderName);

    doReturn(List.of(existingFolder)).when(googleDriveService).getFilesPage(expectedQuery);

    // When
    yearRangeFolderLocator.handle(folderParams);

    // Then
    assertThat(folderParams.getCurrentFolder()).isEqualTo(existingFolder);
    assertThat(folderParams.getDate()).isEqualTo(today);
    assertThat(folderParams.getCurrentFolder().getName()).isEqualTo(folderName);
    verify(googleDriveService).getFilesPage(expectedQuery);
    verify(googleDriveService, never()).createFolder(anyString(), any(File.class));
  }

  @Test
  void testHandle_WhenFolderDoesNotExist_ThenCreateNewFolderAndSwitchToIt() {
    // Given
    var today = LocalDate.now();
    var folderName = get5YearRange(today.getYear());

    var folderParams = FolderParams.builder()
        .date(today)
        .currentFolder(PARENT_FOLDER)
        .build();

    var expectedQuery = "mimeType = '%s' and name = '%s' and trashed = false and '%s' in parents"
        .formatted(FOLDER.getMimeType(), folderName, PARENT_FOLDER.getId());

    var locatedFolder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(folderName)
        .setParents(List.of(PARENT_FOLDER.getId()));

    doReturn(emptyList()).when(googleDriveService).getFilesPage(expectedQuery);
    doReturn(locatedFolder).when(googleDriveService).createFolder(folderName, PARENT_FOLDER);

    // When
    yearRangeFolderLocator.handle(folderParams);

    // Then
    assertThat(folderParams.getCurrentFolder()).isEqualTo(locatedFolder);
    assertThat(folderParams.getDate()).isEqualTo(today);
    assertThat(folderParams.getCurrentFolder().getName()).isEqualTo(folderName);

    verify(googleDriveService).getFilesPage(expectedQuery);
    verify(googleDriveService).createFolder(folderName, PARENT_FOLDER);
  }

}
