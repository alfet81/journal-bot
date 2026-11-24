package com.alfet.journalbot.drive;

import module java.base;

import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.FOLDER;
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
class YearFolderLocatorTest {

  private static final File PARENT_FOLDER = new File()
      .setId(UUID.randomUUID().toString())
      .setName("Parent Folder");

  @Mock
  private GoogleDriveService googleDriveService;

  @InjectMocks
  private YearFolderLocator yearFolderLocator;


  @Test
  void testHandle_WhenFolderExists_ThenSwitchToLocatedFolder() {
    // Given
    var today = LocalDate.now();
    var folderName = String.valueOf(today.getYear());

    var folderParams = FolderParams.builder()
        .date(today)
        .currentFolder(PARENT_FOLDER)
        .build();

    var expectedQuery = "mimeType = '%s' and name = '%s' and trashed = false and '%s' in parents"
        .formatted(FOLDER.getMimeType(), folderName, PARENT_FOLDER.getId());

    var locatedFolder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(folderName);

    doReturn(List.of(locatedFolder)).when(googleDriveService).getFilesPage(expectedQuery);

    // When
    yearFolderLocator.handle(folderParams);

    // Then
    assertThat(folderParams.getCurrentFolder()).isEqualTo(locatedFolder);
    assertThat(folderParams.getDate()).isEqualTo(today);
    assertThat(folderParams.getCurrentFolder().getName()).isEqualTo(folderName);

    verify(googleDriveService).getFilesPage(expectedQuery);
    verify(googleDriveService, never()).createFolder(anyString(), any(File.class));
  }

  @Test
  void testHandle_WhenFolderDoesNotExist_ThenCreateNewFolderAndSwitchToIt() {
    // Given
    var today = LocalDate.now();
    var folderName = String.valueOf(today.getYear());

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
    doReturn(locatedFolder).when(googleDriveService).createFolder(String.valueOf(today.getYear()),
        PARENT_FOLDER);

    // When
    yearFolderLocator.handle(folderParams);

    // Then
    assertThat(folderParams.getCurrentFolder()).isEqualTo(locatedFolder);
    assertThat(folderParams.getDate()).isEqualTo(today);
    assertThat(folderParams.getCurrentFolder().getName()).isEqualTo(folderName);

    verify(googleDriveService).getFilesPage(expectedQuery);
    verify(googleDriveService).createFolder(folderName, PARENT_FOLDER);
  }

}
