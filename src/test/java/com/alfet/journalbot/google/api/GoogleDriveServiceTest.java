package com.alfet.journalbot.google.api;

import module java.base;
import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.DOCUMENT;
import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.FOLDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.util.FileUtils;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleDriveServiceTest {

  private static final String ID_FIELD = "id";
  private static final int PAGE_SIZE = 10;
  private static final String FIELDS = "nextPageToken, files(id, name)";

  private static final String SEARCH_QUERY = "searchQuery";
  private static final String NEXT_PAGE_TOKEN = "nextPageToken";

  private static final String FOLDER_NAME = "ABC";
  private static final String PARENT_FOLDER_ID = "parentId";
  private static final String PARENT_FOLDER_NAME = "parentFolderName";

  private static final String DOC_TITLE = "doc title";
  private static final String DOC_CONTENT = "doc content";

  private final Drive googleDriveApi = mock(Drive.class, RETURNS_DEEP_STUBS);

  @Captor
  private ArgumentCaptor<File> fileMetadataCaptor;

  @Captor
  private ArgumentCaptor<FileContent> fileContentCaptor;

  private GoogleDriveService googleDriveService;

  @BeforeEach
  void setup() {
    googleDriveService = new GoogleDriveService(googleDriveApi);
  }

  @Test
  void getFilesPage_WhenSinglePage() throws IOException {
    // Given
    when(googleDriveApi.files().list().setPageSize(PAGE_SIZE).setPageToken(any()).setQ(SEARCH_QUERY)
        .setFields(FIELDS).execute()).thenReturn(new FileList());

    // When
    var result = googleDriveService.getFilesPage(SEARCH_QUERY);

    // Then
    verify(googleDriveApi.files().list().setPageSize(PAGE_SIZE).setPageToken(any())
        .setQ(SEARCH_QUERY).setFields(FIELDS)).execute();
  }

  @Test
  void getFilesPage_WhenMultiplePages() throws IOException {
    // Given
    when(googleDriveApi.files().list().setPageSize(PAGE_SIZE).setPageToken(isNull())
        .setQ(SEARCH_QUERY).setFields(FIELDS).execute())
        .thenReturn(new FileList().setNextPageToken(NEXT_PAGE_TOKEN).setFiles(List.of(new File())));

    when(googleDriveApi.files().list().setPageSize(PAGE_SIZE).setPageToken(NEXT_PAGE_TOKEN)
        .setQ(SEARCH_QUERY).setFields(FIELDS).execute())
        .thenReturn(new FileList().setFiles(List.of(new File())));

    // When
    var result = googleDriveService.getFilesPage(SEARCH_QUERY);

    // Then
    assertThat(result).hasSize(2);
    verify(googleDriveApi.files().list().setPageSize(PAGE_SIZE).setPageToken(any())
        .setQ(SEARCH_QUERY).setFields(FIELDS)).execute();

    verify(googleDriveApi.files().list().setPageSize(PAGE_SIZE).setPageToken(NEXT_PAGE_TOKEN)
        .setQ(SEARCH_QUERY).setFields(FIELDS)).execute();
  }

  @Test
  void getFilesPage_WhenIOExceptionIsThrown() throws IOException {
    // Given
    var ioException = new IOException();
    when(googleDriveApi.files().list().setPageSize(PAGE_SIZE).setPageToken(isNull())
        .setQ(SEARCH_QUERY).setFields(FIELDS).execute())
        .thenThrow(ioException);

    // When
    var exception = catchException(() -> googleDriveService.getFilesPage(SEARCH_QUERY));

    // Then
    assertThat(exception).isInstanceOf(ApplicationException.class).hasCause(ioException);
    verify(googleDriveApi.files().list().setPageSize(PAGE_SIZE).setPageToken(isNull())
        .setQ(SEARCH_QUERY).setFields(FIELDS)).execute();
  }

  @Test
  void getFilesPage_WhenIllegalArgumentExceptionIsThrown() throws IOException {
    // When
    var exception = catchException(() -> googleDriveService.getFilesPage(null));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("searchQuery is marked non-null but is null");

    verify(googleDriveApi.files().list().setPageSize(anyInt()).setPageToken(any())
        .setQ(anyString()).setFields(anyString()), never()).execute();
  }

  @Test
  void createFolder_WhenParentIsNull() throws IOException {
    // Given
    var expectedFile = new File();
    var expectedFileMetadata = new File()
        .setName(FOLDER_NAME)
        .setMimeType(FOLDER.getMimeType().toString());

    var filesMock = mock(Drive.Files.class);
    var createMock = mock(Drive.Files.Create.class);

    when(googleDriveApi.files()).thenReturn(filesMock);
    when(filesMock.create(any(File.class))).thenReturn(createMock);
    when(createMock.setFields(ID_FIELD)).thenReturn(createMock);
    when(createMock.execute()).thenReturn(expectedFile);

    // When
    var result = googleDriveService.createFolder(FOLDER_NAME, null);

    // Then
    assertThat(result).isEqualTo(expectedFile);
    verify(filesMock).create(fileMetadataCaptor.capture());
    verify(createMock).execute();

    assertThat(fileMetadataCaptor.getValue()).isEqualTo(expectedFileMetadata);
  }

  @Test
  void createFolder_WhenParentIsNotNull() throws IOException {
    // Given
    var expectedFile = new File();
    var expectedFileMetadata = new File()
        .setName(FOLDER_NAME)
        .setMimeType(FOLDER.getMimeType().toString())
        .setParents(List.of(PARENT_FOLDER_ID));

    var filesMock = mock(Drive.Files.class);
    var createMock = mock(Drive.Files.Create.class);

    when(googleDriveApi.files()).thenReturn(filesMock);
    when(filesMock.create(any(File.class))).thenReturn(createMock);
    when(createMock.setFields(ID_FIELD)).thenReturn(createMock);
    when(createMock.execute()).thenReturn(expectedFile);

    // When
    var result = googleDriveService.createFolder(FOLDER_NAME, new File().setId(PARENT_FOLDER_ID));

    // Then
    assertThat(result).isEqualTo(expectedFile);
    verify(filesMock).create(fileMetadataCaptor.capture());
    verify(createMock).execute();

    assertThat(fileMetadataCaptor.getValue()).isEqualTo(expectedFileMetadata);
  }

  @Test
  void createFolder_WhenParentFolderIsNotNullAndIOExceptionIsThrown() throws IOException {
    // Given
    var ioException = new IOException();

    var filesMock = mock(Drive.Files.class);
    var createMock = mock(Drive.Files.Create.class);

    when(googleDriveApi.files()).thenReturn(filesMock);
    when(filesMock.create(any(File.class))).thenReturn(createMock);
    when(createMock.setFields(ID_FIELD)).thenReturn(createMock);
    when(createMock.execute()).thenThrow(ioException);

    // When
    var exception = catchException(() ->
        googleDriveService.createFolder(FOLDER_NAME,
            new File().setId(PARENT_FOLDER_ID).setName(PARENT_FOLDER_NAME)));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasCause(ioException)
        .hasMessage(
            "Failed to created folder with name 'ABC' and parent folder 'parentFolderName'");

    verify(createMock).execute();
  }

  private static Stream<Arguments> provideCreateDocumentFileInvalidTestArgs() {
    return Stream.of(
        arguments("documentTitle is null", null, DOC_CONTENT,
            "Document title cannot be null or empty"),
        arguments("documentContent is null", DOC_TITLE, null,
            "Document content cannot be null or empty")
    );
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideCreateDocumentFileInvalidTestArgs")
  @DisplayName("createDocumentFile_WhenInvalidArgumentsPassed")
  void createDocumentFile_WhenInvalidArgumentsPassed(String scenario, String documentTitle,
      String documentContent, String expectedErrorMessage) throws IOException {
    // When
    var exception = catchException(() -> googleDriveService.createDocumentFile(documentTitle,
        documentContent, null));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(expectedErrorMessage);
  }

  @Test
  void createDocumentFile_WhenParentFolderIsNull() throws IOException {
    // Given
    var expectedDocumentFile = new File();
    var expectedFileMetadata = new File()
        .setName(DOC_TITLE)
        .setMimeType(DOCUMENT.getMimeType().toString());

    var filesMock = mock(Drive.Files.class);
    var createMock = mock(Drive.Files.Create.class);

    when(googleDriveApi.files()).thenReturn(filesMock);
    when(filesMock.create(any(File.class), any(FileContent.class))).thenReturn(createMock);
    when(createMock.setFields(ID_FIELD)).thenReturn(createMock);

    when(createMock.execute()).thenReturn(expectedDocumentFile);

    // When
    var result = googleDriveService.createDocumentFile(DOC_TITLE, DOC_CONTENT, null);

    // Then
    assertThat(result).isEqualTo(expectedDocumentFile);
    verify(filesMock).create(fileMetadataCaptor.capture(), fileContentCaptor.capture());
    verify(createMock).execute();

    assertThat(fileMetadataCaptor.getValue()).isEqualTo(expectedFileMetadata);
    assertThat(fileContentCaptor.getValue().getType()).isEqualTo(TEXT_HTML_VALUE);
    assertThat(fileContentCaptor.getValue().getFile()).isNotNull();
  }


  @Test
  void createFolder_WhenParentFolderIsNullAndIOExceptionIsThrown() throws IOException {
    // Given
    var ioException = new IOException();

    var filesMock = mock(Drive.Files.class);
    var createMock = mock(Drive.Files.Create.class);

    when(googleDriveApi.files()).thenReturn(filesMock);
    when(filesMock.create(any(File.class))).thenReturn(createMock);
    when(createMock.setFields(ID_FIELD)).thenReturn(createMock);
    when(createMock.execute()).thenThrow(ioException);

    // When
    var exception = catchException(() -> googleDriveService.createFolder(FOLDER_NAME, null));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasCause(ioException)
        .hasMessage("Failed to created folder with name 'ABC' and parent folder 'null'");

    verify(createMock).execute();
  }

  @Test
  void createDocumentFile_WhenParentFolderIsNotNull() throws IOException {
    // Given
    var expectedDocumentFile = new File();
    var expectedFileMetadata = new File()
        .setName(DOC_TITLE)
        .setMimeType(DOCUMENT.getMimeType().toString())
        .setParents(List.of(PARENT_FOLDER_ID));

    var filesMock = mock(Drive.Files.class);
    var createMock = mock(Drive.Files.Create.class);

    when(googleDriveApi.files()).thenReturn(filesMock);
    when(filesMock.create(any(File.class), any(FileContent.class))).thenReturn(createMock);
    when(createMock.setFields(ID_FIELD)).thenReturn(createMock);

    when(createMock.execute()).thenReturn(expectedDocumentFile);

    // When
    var result = googleDriveService.createDocumentFile(DOC_TITLE, DOC_CONTENT,
        new File().setId(PARENT_FOLDER_ID));

    // Then
    assertThat(result).isEqualTo(expectedDocumentFile);
    verify(filesMock).create(fileMetadataCaptor.capture(), fileContentCaptor.capture());
    verify(createMock).execute();

    assertThat(fileMetadataCaptor.getValue()).isEqualTo(expectedFileMetadata);
    assertThat(fileContentCaptor.getValue().getType()).isEqualTo(TEXT_HTML_VALUE);
    assertThat(fileContentCaptor.getValue().getFile()).isNotNull();
  }

  @Test
  void createDocumentFile_WhenParentFolderIsNullAndIOExceptionIsThrown() throws IOException {
    // Given
    var ioException = new IOException();

    var filesMock = mock(Drive.Files.class);
    var createMock = mock(Drive.Files.Create.class);

    when(googleDriveApi.files()).thenReturn(filesMock);
    when(filesMock.create(any(File.class), any(FileContent.class))).thenReturn(createMock);
    when(createMock.setFields(ID_FIELD)).thenReturn(createMock);

    when(createMock.execute()).thenThrow(ioException);

    // When
    var exception = catchException(() ->
        googleDriveService.createDocumentFile(DOC_TITLE, DOC_CONTENT, null));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasCause(ioException)
        .hasMessage("Failed to created document with name 'doc title'");

    verify(filesMock).create(any(File.class), any(FileContent.class));
    verify(createMock).execute();
  }

  @Test
  void createDocumentFile_WhenTempFileCanNotBeDeleted() throws IOException {
    try (var fileUtilsMock = mockStatic(FileUtils.class)) {
      // Given
      var tempFile = mock(java.io.File.class);
      when(tempFile.delete()).thenReturn(false);
      fileUtilsMock.when(() -> FileUtils.createTempFile(DOC_TITLE, DOC_CONTENT.getBytes(UTF_8),
          null)).thenReturn(tempFile);

      var filesMock = mock(Drive.Files.class);
      var createMock = mock(Drive.Files.Create.class);

      when(googleDriveApi.files()).thenReturn(filesMock);
      when(filesMock.create(any(File.class), any(FileContent.class))).thenReturn(createMock);
      when(createMock.setFields(ID_FIELD)).thenReturn(createMock);

      // When
      googleDriveService.createDocumentFile(DOC_TITLE, DOC_CONTENT, null);

      // Then
      verify(createMock).execute();
      verify(filesMock).create(any(File.class), any(FileContent.class));
    }
  }

  @Test
  void createDocumentFile_WhenCreateTempFileThrowsIOException() throws IOException {
    try (var fileUtilsMock = mockStatic(FileUtils.class)) {
      // Given
      var causeException = new IOException();
      fileUtilsMock.when(() -> FileUtils.createTempFile(DOC_TITLE, DOC_CONTENT.getBytes(UTF_8),
          null)).thenThrow(causeException);

      // When
      var exception = catchException(() ->
          googleDriveService.createDocumentFile(DOC_TITLE, DOC_CONTENT, null));

      // Then
      assertThat(exception).isInstanceOf(ApplicationException.class).hasCause(causeException);
      verify(googleDriveApi.files(), never()).create(any(File.class), any(FileContent.class));
    }
  }

  @Test
  void createDocumentFile_WhenCreateTempFileThrowsIllegalArgumentException() throws IOException {
    try (var fileUtilsMock = mockStatic(FileUtils.class)) {
      // Given
      var causeException = new IllegalArgumentException();
      fileUtilsMock.when(() -> FileUtils.createTempFile(DOC_TITLE, DOC_CONTENT.getBytes(UTF_8),
          null)).thenThrow(causeException);

      // When
      var exception = catchException(() ->
          googleDriveService.createDocumentFile(DOC_TITLE, DOC_CONTENT, null));

      // Then
      assertThat(exception).isInstanceOf(ApplicationException.class).hasCause(causeException);
      verify(googleDriveApi.files(), never()).create(any(File.class), any(FileContent.class));
    }
  }
}
