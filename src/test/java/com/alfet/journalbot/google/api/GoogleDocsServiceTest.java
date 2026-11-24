package com.alfet.journalbot.google.api;

import module java.base;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alfet.journalbot.exception.ApplicationException;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleDocsServiceTest {

  private static final String DOC_ID = "ABC";
  private static final Map<String, String> MAPPING = Map.of("TEST", "XYZ");

  private final Docs docsApi = mock(Docs.class, RETURNS_DEEP_STUBS);

  private GoogleDocsService googleDocsService;

  @BeforeEach
  void setup() {
    googleDocsService = new GoogleDocsService(docsApi);
  }

  @Test
  void getDocument() throws IOException {
    // Given
    var document = new Document();

    when(docsApi.documents().get(anyString()).execute()).thenReturn(document);

    // When
    var result = googleDocsService.getDocument(DOC_ID);

    // Then
    assertThat(result).isSameAs(document);
    verify(docsApi.documents().get(DOC_ID)).execute();
  }

  @Test
  void getDocument_WhenIOException() throws IOException {
    // Given
    var ioException = new IOException();

    when(docsApi.documents().get(anyString()).execute()).thenThrow(ioException);

    // When
    var exception = catchException(() -> googleDocsService.getDocument(DOC_ID));

    // Then
    assertThat(exception).isInstanceOf(ApplicationException.class).hasCause(ioException);
    verify(docsApi.documents().get(DOC_ID)).execute();
  }


  @Test
  void mergeDocument() throws IOException {
    // Given
    var mockDocuments = mock(Docs.Documents.class);
    var mockBatchUpdate = mock(Docs.Documents.BatchUpdate.class);

    when(docsApi.documents()).thenReturn(mockDocuments);
    when(mockDocuments.batchUpdate(eq(DOC_ID), any(BatchUpdateDocumentRequest.class)))
        .thenReturn(mockBatchUpdate);
    when(mockBatchUpdate.execute()).thenReturn(null);

    // When
    googleDocsService.mergeDocument(DOC_ID, MAPPING);

    // Then
    verify(mockBatchUpdate).execute();
  }

  private static Stream<Arguments> provideMergeDocumentInvalidTestArgs() {
    return Stream.of(
        arguments("documentId is null", null, Map.of(),
            "documentId is marked non-null but is null"),
        arguments("mapping is null", DOC_ID, null, "mapping is marked non-null but is null")
    );
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideMergeDocumentInvalidTestArgs")
  @DisplayName("mergeDocument_WhenIllegalArgumentException")
  void mergeDocument_WhenIllegalArgumentException(String scenario, String docId,
      Map<String, String> mapping, String expectedErrorMessage) throws IOException {

    // Given
    var mockBatchUpdate = mock(Docs.Documents.BatchUpdate.class);

    // When
    var exception = catchException(() -> googleDocsService.mergeDocument(docId, mapping));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(expectedErrorMessage);
    verify(mockBatchUpdate, never()).execute();
  }

  @Test
  void mergeDocument_WhenIOException() throws IOException {
    // Given
    var ioException = new IOException();
    var mockDocuments = mock(Docs.Documents.class);
    var mockBatchUpdate = mock(Docs.Documents.BatchUpdate.class);

    when(docsApi.documents()).thenReturn(mockDocuments);
    when(mockDocuments.batchUpdate(eq(DOC_ID), any(BatchUpdateDocumentRequest.class)))
        .thenReturn(mockBatchUpdate);
    when(mockBatchUpdate.execute()).thenThrow(ioException);

    // When
    var exception = catchException(() ->
        googleDocsService.mergeDocument(DOC_ID, MAPPING));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasCause(ioException);
    verify(mockBatchUpdate).execute();
  }

  @Test
  void mergeDocument_WhenMappingIsEmpty() throws IOException {
    // Given
    var mockBatchUpdate = mock(Docs.Documents.BatchUpdate.class);

    // When
    googleDocsService.mergeDocument(DOC_ID, Map.of());

    // Then
    verify(mockBatchUpdate, never()).execute();
  }
}
