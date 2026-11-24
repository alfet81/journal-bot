package com.alfet.journalbot.atracker.message.processor;

import module java.base;
import static com.alfet.journalbot.message.type.MessageType.A_TRACKER;
import static com.alfet.journalbot.message.type.MessageType.LOG;
import static com.alfet.journalbot.util.FileUtils.FOLDER_SEPARATOR;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alfet.journalbot.atracker.domain.TaskDetails;
import com.alfet.journalbot.atracker.mapper.TaskDetailsMapperImpl;
import com.alfet.journalbot.config.FactoryConfig;
import com.alfet.journalbot.config.JournalBotProperties;
import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.log.domain.LogRecord;
import com.alfet.journalbot.message.domain.EmailMessage;
import com.alfet.journalbot.message.domain.EmailMessageAttachment;
import com.alfet.journalbot.message.domain.EmailMessageMetadata;
import com.alfet.journalbot.message.type.MessageType;
import com.alfet.journalbot.service.JournalManagementService;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@Import(TaskDetailsMapperImpl.class)
@ContextConfiguration(classes = {FactoryConfig.class})
class ATrackerEmailMessageProcessorTest {

  private static final String EMAIL_SENDER = "john.doe@gmail.com";
  private static final String EMAIL_RECIPIENT = "journal.bot@gmail.com";
  private static final String EMAIL_SUBJECT = "log 102626";
  private static final String FILE_NAME = "CSVFile.csv";
  private static final LocalDateTime EMAIL_DATE_TIME = LocalDateTime.of(2025, 10, 4, 21, 43, 57);
  private static final LocalDate EXPECTED_LOG_DATE = LocalDate.of(2025, 10, 4);
  private static final String ATTACHMENT_ID = "XYZ";
  private static final String EMAIL_TEXT = "Some text";
  private static final String MESSAGE_ID = "1a1a02b0b9ad1b53";
  private static final String FILE_ID = "ABC";

  private static final String LOG_CONTENT = """
      03/10/2025 11:50 PM - 12:05 AM (15 min) Hygiene (shower  MW)
      12:05 AM - 12:30 AM (25 min) Web surfing
      12:30 AM - 12:45 AM (15 min) Shopping (amazon)
      12:45 AM - 1:15 AM (30 min) Social Media
      1:15 AM - 9:20 AM (8 hr 5 min) Sleep
      9:20 AM - 9:40 AM (20 min) Gaming (chess)
      9:40 AM - 10:10 AM (30 min) Social Media
      10:10 AM - 10:30 AM (20 min) Shopping (amazon)
      10:30 AM - 10:35 AM (5 min) Hygiene (MW)
      10:35 AM - 11:05 AM (30 min) Routine
      11:05 AM - 12:05 PM (1 hr) Cooking (veggie stew  buckwheat)
      12:05 PM - 1:05 PM (1 hr) Kitchen errands (dish wash  range  etc)
      1:05 PM - 1:25 PM (20 min) Eating (buckwheat  ssg  VS)
      1:25 PM - 1:35 PM (10 min) Hygiene (washlet)
      1:35 PM - 1:45 PM (10 min) Break
      1:45 PM - 3:15 PM (1 hr 30 min) Landscaping (blowing  mowing)
      3:15 PM - 3:40 PM (25 min) Pool maintenance (robot cleaning)
      3:40 PM - 4:10 PM (30 min) Family (parents)
      4:10 PM - 5:45 PM (1 hr 35 min) Landscaping (trimming  edging  blowing)
      5:45 PM - 8:20 PM (2 hr 35 min) Pool maintenance (filter cleaning  robot cleaning  chemicals)
      8:20 PM - 8:40 PM (20 min) Break
      8:40 PM - 8:50 PM (10 min) Hygiene (shower)
      8:50 PM - 9:10 PM (20 min) Web surfing
      9:10 PM - 10:10 PM (1 hr) TV
      10:10 PM - 10:30 PM (20 min) Kitchen errands (dish wash)
      10:30 PM - 10:35 PM (5 min) Routine
      10:35 PM - 10:50 PM (15 min) Driving (marathon  home)
      10:50 PM - 05/10/2025 12:00 AM (1 hr 10 min) Family
      """;

  @TempDir
  private Path tempDir;

  @Value("classpath:csv/CSVFile.csv")
  private Resource csvFileResource;

  @Captor
  private ArgumentCaptor<LogRecord> logRecordCaptor;

  @Mock
  private JournalBotProperties journalBotProperties;

  @Mock
  private JournalManagementService journalManagementService;

  @Autowired
  private Function<CSVRecord, TaskDetails> taskDetailsFactory;

  private ATrackerEmailMessageProcessor atrackerEmailMessageProcessor;

  private static Stream<Arguments> provideProcessEmailMessageTestArgs() {
    return Stream.of(
        arguments("Valid email message subject", EMAIL_SUBJECT),
        arguments("Empty email message subject", EMPTY),
        arguments("Invalid email message subject", "INVALID")
    );
  }

  @BeforeEach
  void init() {
    atrackerEmailMessageProcessor = new ATrackerEmailMessageProcessor(journalBotProperties,
        journalManagementService, taskDetailsFactory);
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideProcessEmailMessageTestArgs")
  @DisplayName("process_WhenValidEmailMessage_WithSubject")
  void process_WhenValidEmailMessage_WithSubject(String scenario, String subject)
      throws IOException {
    // Given
    var emailMessage = buildEmailMessage(A_TRACKER, subject, loadCsvFile());
    var expectedContent = StringUtils.strip(LOG_CONTENT,"\n") + "\n\n" + tempDir + FOLDER_SEPARATOR
        + "20251004" + FOLDER_SEPARATOR + FILE_NAME + "\n\n";

    when(journalBotProperties.getAttachmentsRootDir())
        .thenReturn(tempDir.toAbsolutePath().toString());

    doNothing().when(journalManagementService).processLogRecord(any(LogRecord.class));

    // When
    atrackerEmailMessageProcessor.process(emailMessage);

    // Then
    verify(journalManagementService).processLogRecord(logRecordCaptor.capture());
    var logRecord = logRecordCaptor.getValue();
    assertThat(logRecord).isNotNull();
    assertThat(logRecord.date()).isEqualTo(EXPECTED_LOG_DATE);
    assertThat(logRecord.content()).isEqualTo(expectedContent);
  }

  @Test
  void process_WhenEmailMessageIsNull() {
    // When
    var exception = catchException(() -> atrackerEmailMessageProcessor.process(null));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("emailMessage is marked non-null but is null");
    verify(journalManagementService, never()).processLogRecord(any(LogRecord.class));
  }

  @Test
  void process_WhenEmailMessageTypeIsInvalid() {
    // When
    var emailMessage = buildEmailMessage(LOG, EMAIL_SUBJECT, null);

    var exception = catchException(() -> atrackerEmailMessageProcessor.process(emailMessage));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasMessage("Message type 'LOG' is not supported");
    verify(journalManagementService, never()).processLogRecord(any(LogRecord.class));
  }

  @Test
  void process_WhenEmailMessageCsvAttachmentIsMissing() {
    // When
    var emailMessage = buildEmailMessage(A_TRACKER, EMAIL_SUBJECT, null);
    ReflectionTestUtils.setField(emailMessage, "attachments", List.of());

    var exception = catchException(() -> atrackerEmailMessageProcessor.process(emailMessage));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasMessage("Could not find CSV file in the list of email "
            + "attachments or content is missing.");

    verify(journalManagementService, never()).processLogRecord(any(LogRecord.class));
  }

  @Test
  void process_WhenSaveEmailMessageAttachmentFailsWithIOException() throws IOException {
    // Given
    var emailMessage = buildEmailMessage(A_TRACKER, EMAIL_SUBJECT, loadCsvFile());
    when(journalBotProperties.getAttachmentsRootDir()).thenReturn("/invalid");

    // When
    var exception = catchException(() -> atrackerEmailMessageProcessor.process(emailMessage));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasCauseInstanceOf(IOException.class)
        .hasMessage("Failed to save email message attachment with file name 'CSVFile.csv' "
            + "into folder '/invalid/20251004'");

    verify(journalManagementService, never()).processLogRecord(any(LogRecord.class));
  }

  private static EmailMessage buildEmailMessage(MessageType messageType, String subject,
      byte[] content) {
    var metadata = EmailMessageMetadata.builder()
        .recipient(EMAIL_RECIPIENT)
        .sender(EMAIL_SENDER)
        .subject(subject)
        .dateTime(EMAIL_DATE_TIME)
        .build();
    var attachment = EmailMessageAttachment.builder()
        .id(ATTACHMENT_ID)
        .fileId(FILE_ID)
        .fileName(FILE_NAME)
        .content(content)
        .build();
    return EmailMessage.builder()
        .id(MESSAGE_ID)
        .type(messageType)
        .text(EMAIL_TEXT)
        .metadata(metadata)
        .attachments(List.of(attachment))
        .build();
  }

  private byte[] loadCsvFile() throws IOException {
    return csvFileResource.getContentAsByteArray();
  }
}
