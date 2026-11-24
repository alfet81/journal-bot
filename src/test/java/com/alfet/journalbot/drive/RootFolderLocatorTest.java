package com.alfet.journalbot.drive;

import module java.base;

import static com.alfet.journalbot.drive.RootFolderLocator.ROOT_FOLDER_NAME;
import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.FOLDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static java.util.Collections.emptyList;

import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.google.api.GoogleDriveService;
import com.google.api.services.drive.model.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RootFolderLocatorTest {

  @Mock
  private GoogleDriveService googleDriveService;

  @InjectMocks
  private RootFolderLocator rootFolderLocator;


  @Test
  void testHandle_WhenFolderExists_ThenSwitchToLocatedFolder() {
    // Given
    var today = LocalDate.now();

    var folderParams = FolderParams.builder()
        .date(today)
        .build();

    var expectedQuery = "mimeType = '%s' and name = '%s' and trashed = false"
        .formatted(FOLDER.getMimeType(), ROOT_FOLDER_NAME);

    var locatedFolder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(ROOT_FOLDER_NAME);

    doReturn(List.of(locatedFolder)).when(googleDriveService).getFilesPage(expectedQuery);

    // When
    rootFolderLocator.handle(folderParams);

    // Then
    assertThat(folderParams.getCurrentFolder()).isEqualTo(locatedFolder);
    assertThat(folderParams.getDate()).isEqualTo(today);
    assertThat(folderParams.getCurrentFolder().getName()).isEqualTo(ROOT_FOLDER_NAME);

    verify(googleDriveService).getFilesPage(expectedQuery);
    verify(googleDriveService, never()).createFolder(anyString(), any(File.class));
  }

  @Test
  void testHandle_WhenFolderDoesNotExist_ThenThrowApplicationException() {
    // Given
    var today = LocalDate.now();

    var folderParams = FolderParams.builder()
        .date(today)
        .build();

    var expectedQuery = "mimeType = '%s' and name = '%s' and trashed = false"
        .formatted(FOLDER.getMimeType(), ROOT_FOLDER_NAME);

    doReturn(emptyList()).when(googleDriveService).getFilesPage(expectedQuery);

    // When
    var exception = catchException(() -> rootFolderLocator.handle(folderParams));

    // Then
    assertThat(exception).isInstanceOf(ApplicationException.class);

    verify(googleDriveService).getFilesPage(expectedQuery);
    verify(googleDriveService, never()).createFolder(anyString(), any(File.class));
  }
}
