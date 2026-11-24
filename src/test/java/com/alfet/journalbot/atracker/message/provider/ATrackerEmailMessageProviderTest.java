package com.alfet.journalbot.atracker.message.provider;

import module java.base;
import static com.alfet.journalbot.google.api.types.GmailMimeType.MULTIPART_MIXED;
import static com.alfet.journalbot.google.api.types.GmailMimeType.TEXT_CSV;
import static com.alfet.journalbot.message.gmail.GmailHeader.DATE;
import static com.alfet.journalbot.message.gmail.GmailHeader.FROM;
import static com.alfet.journalbot.message.gmail.GmailHeader.SUBJECT;
import static com.alfet.journalbot.message.gmail.GmailHeader.TO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.alfet.journalbot.config.JournalBotProperties;
import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.google.api.GmailService;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(JournalBotProperties.class)
class ATrackerEmailMessageProviderTest {

  private static final String GMAIL_QUERY = "label:adp/atracker is:unread";
  private static final String EMAIL_SENDER = "john.doe@gmail.com";
  private static final String EMAIL_RECIPIENT = "journal.bot@gmail.com";
  private static final String EMAIL_SUBJECT = "Task Detail (Oct 4, 2025)";
  private static final String EMAIL_DATE_TIME = "Sat, 4 Oct 2025 21:43:57 -0400";
  private static final String CSV_ATTACHMENT_FILE_NAME = "CSVFile.csv";
  private static final String MESSAGE_ID = "ABC";
  private static final String ATTACHMENT_ID = "XYZ";
  private static final String EMAIL_TEXT = "Some text";

  @Value("classpath:csv/CSVFile.csv")
  private Resource csvFileResource;

  @MockitoBean
  private GmailService gmailService;

  @Autowired
  private ATrackerEmailMessageProvider aTrackerEmailMessageProvider;

  @BeforeEach
  void setup() throws IOException {
    when(gmailService.getMessages(GMAIL_QUERY)).thenReturn(createMessages(createHeaders()));
    when(gmailService.getMessageAttachmentContent(MESSAGE_ID, ATTACHMENT_ID))
        .thenReturn(loadCsvFile().getBytes(UTF_8));
  }

  @Test
  void getEmailMessages_WhenNoMessagesFetched_ThenEmptyResult() throws IOException {
    // Given
    when(gmailService.getMessages(GMAIL_QUERY)).thenReturn(List.of());

    // When
    var emailMessages = aTrackerEmailMessageProvider.getEmailMessages();

    // Then
    assertThat(emailMessages).isEmpty();

    verify(gmailService).getMessages(GMAIL_QUERY);
    verify(gmailService, never()).getMessageAttachmentContent(anyString(), anyString());
  }


  @Test
  void getEmailMessages_WhenMessagesFetched_ThenNonEmptyResult() throws IOException {
    // When
    var emailMessages = aTrackerEmailMessageProvider.getEmailMessages();

    // Then
    assertThat(emailMessages).isNotEmpty();
    var emailMessage = emailMessages.getFirst();

    var metadata = emailMessage.getMetadata();
    assertThat(metadata).isNotNull();
    assertThat(metadata.getSender()).isEqualTo(EMAIL_SENDER);
    assertThat(metadata.getRecipient()).isEqualTo(EMAIL_RECIPIENT);
    assertThat(metadata.getSubject()).isEqualTo(EMAIL_SUBJECT);
    assertThat(metadata.getDateTime()).isEqualTo(LocalDateTime.of(2025, 10, 4, 21, 43, 57));

    assertThat(emailMessage.getText()).isEqualTo(EMAIL_TEXT);

    assertThat(emailMessage.getAttachments()).isNotEmpty().hasSize(1);

    var attachment = emailMessage.getAttachments().getFirst();
    assertThat(attachment.getFileName()).isEqualTo(CSV_ATTACHMENT_FILE_NAME);
    assertThat(attachment.getFullFileName()).isEqualTo(CSV_ATTACHMENT_FILE_NAME);

    assertThat(emailMessage.getDateTime()).isEqualTo(LocalDateTime.of(2025, 10, 4, 21, 43, 57));

    //var attachmentPath = Path.of(attachment.getFullFileName());
    //assertThat(Files.exists(attachmentPath)).isTrue();

    verify(gmailService).getMessages(anyString());
    verify(gmailService).getMessageAttachmentContent(anyString(), anyString());
  }


  @Test
  void getEmailMessages_WhenMessageHeadersAreMissing() throws IOException {
    // Given
    when(gmailService.getMessages(anyString())).thenReturn(createMessages(List.of()));

    // When
    var emailMessages = aTrackerEmailMessageProvider.getEmailMessages();

    // Then
    assertThat(emailMessages).isEmpty();

    verify(gmailService).getMessages(anyString());
    verify(gmailService, never()).getMessageAttachmentContent(anyString(), anyString());
  }

  @Test
  void getEmailMessages_WhenEmailSubjectHeaderIsMissing() throws IOException {
    // Given
    var emailHeaders = createHeaders();
    emailHeaders = emailHeaders.stream()
        .filter((var header) -> !header.getName().equals(SUBJECT.name()))
        .toList();
    var dateHeader = emailHeaders.stream()
        .filter((var header) -> header.getName().equals(DATE.name()))
        .findFirst().orElseThrow();
    dateHeader.setValue("Sun, 5 Oct 2025 23:55:00 -0400");

    when(gmailService.getMessages(GMAIL_QUERY)).thenReturn(createMessages(emailHeaders));

    // When
    var emailMessages = aTrackerEmailMessageProvider.getEmailMessages();

    // Then
    assertThat(emailMessages).isNotEmpty().hasSize(1);

    verify(gmailService).getMessages(GMAIL_QUERY);
    verify(gmailService).getMessageAttachmentContent(MESSAGE_ID, ATTACHMENT_ID);
  }

/*
  @Test
  void getEmailMessages_WhenCsvFileAttachmentIsMissing() throws IOException {
    // Given
    var emailHeaders = createHeaders();
    var messages = createMessages(emailHeaders);
    messages.getFirst().getPayload().setParts(List.of());

    when(gmailService.getMessages(anyString())).thenReturn(messages);

    // When
    var emailMessages = aTrackerEmailMessageProvider.getEmailMessages();

    // Then
    assertThat(emailMessages).isEmpty();

    verify(gmailService).getMessages(anyString());
    verify(gmailService, never()).getMessageAttachmentContent(anyString(), anyString());
  }*/

  @Test
  void getEmailMessages_WhenMessagesFetchingFailedWithApplicationException() throws IOException {
    // Given
    doThrow(new ApplicationException("Something went wrong"))
        .when(gmailService).getMessages(anyString());

    // When
    var emailMessages = aTrackerEmailMessageProvider.getEmailMessages();

    // Then
    assertThat(emailMessages).isEmpty();

    verify(gmailService).getMessages(anyString());
    verify(gmailService, never()).getMessageAttachmentContent(anyString(), anyString());
  }

  @Test
  void getEmailMessages_WhenAttachmentDownloadingFailedWithApplicationException()
      throws IOException {
    // Given
    doThrow(new IOException("Something went wrong"))
        .when(gmailService).getMessageAttachmentContent(MESSAGE_ID, ATTACHMENT_ID);

    // When
    var emailMessages = aTrackerEmailMessageProvider.getEmailMessages();

    // Then
    assertThat(emailMessages).isNotEmpty().hasSize(1);
    assertThat(emailMessages.getFirst().getAttachments()).isEmpty();

    verify(gmailService).getMessages(GMAIL_QUERY);
    verify(gmailService).getMessageAttachmentContent(MESSAGE_ID, ATTACHMENT_ID);
  }
/*
  @Test
  void getEmailMessages_WhenAttachmentSavingFailedWithIOException() throws IOException {
    // Given
    var causeException = new IOException("Something went wrong");
    when(gmailService.getMessageAttachmentContent(anyString(), anyString()))
        .thenThrow(causeException);

    // When
    var emailMessages = aTrackerEmailMessageProvider.getEmailMessages();

    // Then
    assertThat(emailMessages).isEmpty();

    verify(gmailService).getMessages(anyString());
    verify(gmailService).getMessageAttachmentContent(anyString(), anyString());
  }*/

  private static List<Message> createMessages(List<MessagePartHeader> headers) {
    var message = new Message();
    var payload = new MessagePart();

    payload.setHeaders(headers);

    var csvAttachment = new MessagePart();
    csvAttachment.setMimeType(TEXT_CSV.getMimeType().toString());
    csvAttachment.setFilename(CSV_ATTACHMENT_FILE_NAME);
    csvAttachment.setPartId("ABC");
    csvAttachment.setBody(new MessagePartBody().setAttachmentId("XYZ"));

    var mixedMultipart = new MessagePart();
    mixedMultipart.setMimeType(MULTIPART_MIXED.getMimeType().toString());
    mixedMultipart.setParts(List.of(csvAttachment));
    payload.setParts(List.of(mixedMultipart));

    message.setId(MESSAGE_ID);
    message.setPayload(payload);
    message.setSnippet(EMAIL_TEXT);
    return List.of(message);
  }

  private static MessagePartHeader createHeader(String name, String value) {
    return new MessagePartHeader().setName(name).setValue(value);
  }

  private static List<MessagePartHeader> createHeaders() {
    var toHeader = createHeader(TO.name(), EMAIL_RECIPIENT);
    var fromHeader = createHeader(FROM.name(), EMAIL_SENDER);
    var subjectHeader = createHeader(SUBJECT.name(), EMAIL_SUBJECT);
    var dateHeader = createHeader(DATE.name(), EMAIL_DATE_TIME);
    return List.of(toHeader, fromHeader, subjectHeader, dateHeader);
  }

  private String loadCsvFile() throws IOException {
    return csvFileResource.getContentAsString(UTF_8);
  }
}
