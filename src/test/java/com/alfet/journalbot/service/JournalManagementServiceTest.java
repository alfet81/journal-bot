package com.alfet.journalbot.service;

import module java.base;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.FILE_ID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alfet.journalbot.drive.DocumentFolderLocator;
import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.google.api.GoogleDocsService;
import com.alfet.journalbot.google.api.GoogleDriveService;
import com.alfet.journalbot.html.DetailsDocumentGenerator;
import com.alfet.journalbot.log.domain.LogRecord;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.drive.model.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JournalManagementServiceTest {

  private static final String EMAIL_TEXT = "test";
  private static final LocalDate PROCESSING_DATE = LocalDate.now();

  @Mock
  private GoogleDriveService googleDriveService;

  @Mock
  private GoogleDocsService googleDocsService;

  @Mock
  private DetailsDocumentGenerator detailsDocumentGenerator;

  @Mock
  private DocumentFolderLocator documentFolderLocator;

  @InjectMocks
  private JournalManagementService journalManagementService;

  @Test
  void processLogRecord_WhenDocumentExists() {
    // Given
    var logRecord = LogRecord.of(PROCESSING_DATE, EMAIL_TEXT);

    when(googleDriveService.getFilesPage(anyString()))
        .thenReturn(List.of(new File().setId(FILE_ID)));
    when(googleDocsService.getDocument(FILE_ID))
        .thenReturn(new Document().setDocumentId("ABC").setTitle("TEST"));

    // When
    journalManagementService.processLogRecord(logRecord);

    // Then
    verify(googleDriveService).getFilesPage(anyString());
    verify(googleDocsService).getDocument(anyString());
    verify(detailsDocumentGenerator, never()).generate(any(LocalDate.class));
    verify(documentFolderLocator, never()).locateFolder(any(LocalDate.class));
  }

  @Test
  void processLogRecord_WhenDocumentDoesNotExist() {
    // Given
    var logRecord = LogRecord.of(PROCESSING_DATE, EMAIL_TEXT);

    when(googleDriveService.getFilesPage(anyString())).thenReturn(List.of());
    when(detailsDocumentGenerator.generate(any(LocalDate.class))).thenReturn("TEST");
    when(documentFolderLocator.locateFolder(any(LocalDate.class)))
        .thenReturn(Optional.of(new File().setId(FILE_ID)));
    when(googleDriveService.createDocumentFile(anyString(), anyString(), any(File.class)))
        .thenReturn(new File().setId(FILE_ID));
    when(googleDocsService.getDocument(anyString()))
        .thenReturn(new Document().setDocumentId("ABC").setTitle("TEST"));

    // When
    journalManagementService.processLogRecord(logRecord);

    // Then
    verify(googleDriveService).getFilesPage(anyString());
    verify(detailsDocumentGenerator).generate(any(LocalDate.class));
    verify(documentFolderLocator).locateFolder(any(LocalDate.class));
    verify(googleDriveService).createDocumentFile(anyString(), anyString(), any(File.class));
    verify(googleDocsService).getDocument(anyString());
  }

  @Test
  void processLogRecord_WhenDocumentDoesNotExistAndApplicationExceptionThrown() {
    // Given
    var logRecord = LogRecord.of(PROCESSING_DATE, EMAIL_TEXT);

    when(googleDriveService.getFilesPage(anyString())).thenReturn(List.of());
    when(detailsDocumentGenerator.generate(any(LocalDate.class))).thenReturn("TEST");
    when(documentFolderLocator.locateFolder(any(LocalDate.class))).thenReturn(Optional.empty());

    // When
    journalManagementService.processLogRecord(logRecord);

    // Then
    verify(googleDriveService).getFilesPage(anyString());
    verify(detailsDocumentGenerator).generate(any(LocalDate.class));
    verify(documentFolderLocator).locateFolder(any(LocalDate.class));
    verify(googleDriveService, never())
        .createDocumentFile(anyString(), anyString(), any(File.class));
    verify(googleDocsService, never()).getDocument(anyString());
  }

  @Test
  void processLogRecord_WhenGetDocumentFailsWithApplicationExceptionThrown() {
    // Given
    var logRecord = LogRecord.of(PROCESSING_DATE, EMAIL_TEXT);

    when(googleDriveService.getFilesPage(anyString()))
        .thenReturn(List.of(new File().setId(FILE_ID)));

    doThrow(new ApplicationException("Something went wrong"))
        .when(googleDocsService).getDocument(FILE_ID);

    // When
    assertThatCode(() -> journalManagementService.processLogRecord(logRecord))
        .doesNotThrowAnyException();

    // Then
    verify(googleDriveService).getFilesPage(anyString());
    verify(googleDocsService).getDocument(anyString());
  }
}
