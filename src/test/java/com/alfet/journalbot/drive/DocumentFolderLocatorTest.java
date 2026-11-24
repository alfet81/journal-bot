package com.alfet.journalbot.drive;

import module java.base;

import static com.alfet.journalbot.drive.RootFolderLocator.ROOT_FOLDER_NAME;
import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.FOLDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.Mockito.doReturn;
import static java.util.Collections.emptyList;

import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.google.api.GoogleDriveService;
import com.alfet.journalbot.util.MiscUtils;
import com.google.api.services.drive.model.File;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@TestPropertySource(properties = "logging.level.root=DEBUG")
class DocumentFolderLocatorTest {

  private static final String ROOT_FOLDER_SEARCH_QUERY
      = "mimeType = '%s' and name = '%s' and trashed = false";

  private static final String SUB_FOLDER_SEARCH_QUERY
      = ROOT_FOLDER_SEARCH_QUERY + " and '%s' in parents";

  @Mock
  private GoogleDriveService googleDriveService;

  @InjectMocks
  private RootFolderLocator rootFolderLocator;

  @InjectMocks
  private YearRangeFolderLocator yearRangeFolderLocator;

  @InjectMocks
  private YearFolderLocator yearFolderLocator;

  @InjectMocks
  private MonthFolderLocator monthFolderLocator;

  private DocumentFolderLocator documentFolderLocator;

  @BeforeEach
  void setup() {
    documentFolderLocator = new DocumentFolderLocator(rootFolderLocator, yearRangeFolderLocator,
        yearFolderLocator, monthFolderLocator);

    documentFolderLocator.init();
  }

  @Test
  void testLocateFolder_WhenAlreadyExists() {
    // Given
    var documentDate = LocalDate.of(2025, 12, 27);

    var rootFolder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(ROOT_FOLDER_NAME);

    var level1Folder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(MiscUtils.get5YearRange(documentDate.getYear()));

    var level2Folder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(String.valueOf(documentDate.getYear()));

    var level3Folder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(StringUtils.capitalize(documentDate.getMonth().name().toLowerCase()));

    var rootFolderSearchQuery = ROOT_FOLDER_SEARCH_QUERY.formatted(FOLDER.getMimeType(),
        rootFolder.getName());

    var yearRangeFolderSearchQuery = SUB_FOLDER_SEARCH_QUERY.formatted(FOLDER.getMimeType(),
        level1Folder.getName(), rootFolder.getId());

    var yearFolderSearchQuery = SUB_FOLDER_SEARCH_QUERY.formatted(FOLDER.getMimeType(),
        level2Folder.getName(), level1Folder.getId());

    var monthFolderSearchQuery = SUB_FOLDER_SEARCH_QUERY.formatted(FOLDER.getMimeType(),
        level3Folder.getName(), level2Folder.getId());

    doReturn(List.of(rootFolder)).when(googleDriveService).getFilesPage(rootFolderSearchQuery);
    doReturn(List.of(level1Folder))
        .when(googleDriveService).getFilesPage(yearRangeFolderSearchQuery);
    doReturn(List.of(level2Folder)).when(googleDriveService).getFilesPage(yearFolderSearchQuery);
    doReturn(List.of(level3Folder)).when(googleDriveService).getFilesPage(monthFolderSearchQuery);

    // When
    var optionalFolder = documentFolderLocator.locateFolder(documentDate);

    // Then
    assertThat(optionalFolder).isPresent();
    var folder = optionalFolder.get();
    assertThat(folder).isEqualTo(level3Folder);
  }


  @Test
  void testLocateFolder_WhenDoesNotExist_ThenShouldBeCreated() {
    // Given
    var documentDate = LocalDate.of(2025, 12, 27);

    var rootFolder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(ROOT_FOLDER_NAME);

    var level1Folder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(MiscUtils.get5YearRange(documentDate.getYear()))
        .setParents(List.of(rootFolder.getId()));

    var level2Folder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(String.valueOf(documentDate.getYear()))
        .setParents(List.of(level1Folder.getId()));

    var level3Folder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(StringUtils.capitalize(documentDate.getMonth().name().toLowerCase()))
        .setParents(List.of(level2Folder.getId()));

    var rootFolderSearchQuery = ROOT_FOLDER_SEARCH_QUERY.formatted(FOLDER.getMimeType(),
        rootFolder.getName());

    var yearRangeFolderSearchQuery = SUB_FOLDER_SEARCH_QUERY.formatted(FOLDER.getMimeType(),
        level1Folder.getName(), level1Folder.getParents().getFirst());

    var yearFolderSearchQuery = SUB_FOLDER_SEARCH_QUERY.formatted(FOLDER.getMimeType(),
        level2Folder.getName(), level2Folder.getParents().getFirst());

    var monthFolderSearchQuery = SUB_FOLDER_SEARCH_QUERY.formatted(FOLDER.getMimeType(),
        level3Folder.getName(), level3Folder.getParents().getFirst());

    doReturn(List.of(rootFolder)).when(googleDriveService).getFilesPage(rootFolderSearchQuery);
    doReturn(emptyList()).when(googleDriveService).getFilesPage(yearRangeFolderSearchQuery);
    doReturn(emptyList()).when(googleDriveService).getFilesPage(yearFolderSearchQuery);
    doReturn(emptyList()).when(googleDriveService).getFilesPage(monthFolderSearchQuery);

    doReturn(level1Folder).when(googleDriveService)
        .createFolder(level1Folder.getName(), rootFolder);
    doReturn(level2Folder).when(googleDriveService)
        .createFolder(level2Folder.getName(), level1Folder);
    doReturn(level3Folder).when(googleDriveService)
        .createFolder(level3Folder.getName(), level2Folder);

    // When
    var optionalFolder = documentFolderLocator.locateFolder(documentDate);

    // Then
    assertThat(optionalFolder).isPresent();
    var folder = optionalFolder.get();
    assertThat(folder).isEqualTo(level3Folder);
  }

  @Test
  void testLocateFolder_WhenRootFolderDoesNotExist_ThenApplicationException() {
    // Given
    var documentDate = LocalDate.of(2025, 12, 27);

    var rootFolder = new File()
        .setId(UUID.randomUUID().toString())
        .setName(ROOT_FOLDER_NAME);

    var rootFolderSearchQuery = ROOT_FOLDER_SEARCH_QUERY.formatted(FOLDER.getMimeType(),
        rootFolder.getName());

    doReturn(emptyList()).when(googleDriveService).getFilesPage(rootFolderSearchQuery);

    // When
    var exception = catchException(() -> documentFolderLocator.locateFolder(documentDate));

    // Then
    var expectedMessage = "Root folder '%s' is missing".formatted(ROOT_FOLDER_NAME);
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasMessage(expectedMessage);
  }
}
