package com.alfet.journalbot.atracker.message.gmail;

import module java.base;
import static com.alfet.journalbot.message.gmail.GmailHeader.DATE;
import static com.alfet.journalbot.message.gmail.GmailHeader.FROM;
import static com.alfet.journalbot.message.gmail.GmailHeader.SUBJECT;
import static com.alfet.journalbot.message.gmail.GmailHeader.TO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.alfet.journalbot.google.api.types.GmailMimeType;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.junit.jupiter.api.Test;

class ATrackerGmailMessageTest {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss XX");
  private static final ZoneId EDT_ZONE_ID = ZoneId.of("America/New_York");

  private static final String DATE_TIME = "Fri, 23 Oct 2026 21:43:57 -0400";
  private static final LocalDateTime EXPECTED_DATE_TIME = ZonedDateTime.parse(DATE_TIME,
      DATE_TIME_FORMATTER).withZoneSameInstant(EDT_ZONE_ID).toLocalDateTime();
  private static final String TO_EMAIL = "john.doe@gmail.com";
  private static final String FROM_EMAIL = "paul.young@gmail.com";
  private static final String EMAIL_SUBJECT = "Task Details";
  private static final String PLAIN_TEXT_EMAIL_BODY = "This is a test message from text/plain part";
  private static final String PAYLOD_EMAIL_BODY = "This is a test message from payload body";
  private static final String SNIPPET_EMAIL_BODY = "This is a test message from snippet body";
  private static final String ATTACHMENT_ID = "01a1715d-37e3-7246-befc-537afe826327";
  private static final String PART_ID = "01a1715d-ef9b-7072-a938-ae48609568da";
  private static final String FILE_NAME = "TaskDetails.csv";
  private static final String MESSAGE_ID = "1a1a02b0b9ad1b53";

  private static List<MessagePartHeader> buildMessageHeaders() {
    var toHeader = new MessagePartHeader().setName(TO.name()).setValue(TO_EMAIL);
    var fromHeader = new MessagePartHeader().setName(FROM.name()).setValue(FROM_EMAIL);
    var subjectHeader = new MessagePartHeader().setName(SUBJECT.name()).setValue(EMAIL_SUBJECT);
    var dateHeader = new MessagePartHeader().setName(DATE.name()).setValue(DATE_TIME);
    return List.of(toHeader, fromHeader, subjectHeader, dateHeader);
  }

  private static MessagePart buildMessagePayload() {
    var payload = new MessagePart();
    payload.setHeaders(buildMessageHeaders());
    payload.setParts(buildMessageAttachments());
    return payload;
  }

  private static List<MessagePart> buildMessageAttachments() {
    List<MessagePart> attachments = new ArrayList<>();

    var textPlainMessagePart = buildTextPlainMessagePart();
    var multipartMessagePart = buildMultipartMessagePart();

    attachments.add(textPlainMessagePart);
    attachments.add(multipartMessagePart);

    return attachments;
  }

  private static MessagePart buildMultipartMessagePart() {
    var multipartMessagePart = new MessagePart();
    multipartMessagePart.setMimeType(GmailMimeType.MULTIPART_MIXED.getMimeType().toString());

    var csvAttachmentPart = new MessagePart();
    csvAttachmentPart.setMimeType(GmailMimeType.TEXT_CSV.getMimeType().toString());
    csvAttachmentPart.setPartId(PART_ID);
    csvAttachmentPart.setFilename(FILE_NAME);
    var body = new MessagePartBody();
    body.setAttachmentId(ATTACHMENT_ID);
    csvAttachmentPart.setBody(body);

    multipartMessagePart.setParts(List.of(csvAttachmentPart));
    return multipartMessagePart;
  }

  private static MessagePart buildTextPlainMessagePart() {
    var textPlainMessagePart = new MessagePart();
    textPlainMessagePart.setMimeType(GmailMimeType.TEXT_PLAIN.getMimeType().toString());
    var messageBody = createMessageBody(PLAIN_TEXT_EMAIL_BODY);
    textPlainMessagePart.setBody(messageBody);
    return textPlainMessagePart;
  }

  private static MessagePartBody createMessageBody(String messageBodyText) {
    var messageBody = new MessagePartBody();
    var bodyBytes = messageBodyText.getBytes(StandardCharsets.UTF_8);
    var encodedBodyBytes = org.apache.commons.codec.binary.Base64.encodeBase64(bodyBytes);
    messageBody.setData(new String(encodedBodyBytes, UTF_8));
    return messageBody;
  }

  private static Message buildMessage() {
    Message message = new Message().setId(MESSAGE_ID);
    message.setPayload(buildMessagePayload());
    return message;
  }

  @Test
  void getMetadata_WhenDateTimeProvided() {
    // Given
    var message = buildMessage();
    var gmailMessage = new ATrackerGmailMessage(message);

    // When
    var metadata = gmailMessage.getMetadata();

    // Then
    assertThat(metadata.getSubject()).isEqualTo(EMAIL_SUBJECT);
    assertThat(metadata.getSender()).isEqualTo(FROM_EMAIL);
    assertThat(metadata.getRecipient()).isEqualTo(TO_EMAIL);
    assertThat(metadata.getDateTime()).isEqualTo(EXPECTED_DATE_TIME);
  }


  @Test
  void getMetadata_WhenDateTimeIsMissing() {
    // Given
    var message = buildMessage();
    message.getPayload().getHeaders().getLast().setValue(null);
    var gmailMessage = new ATrackerGmailMessage(message);

    // When
    var metadata = gmailMessage.getMetadata();

    // Then
    assertThat(metadata.getSubject()).isEqualTo(EMAIL_SUBJECT);
    assertThat(metadata.getSender()).isEqualTo(FROM_EMAIL);
    assertThat(metadata.getRecipient()).isEqualTo(TO_EMAIL);
    assertThat(metadata.getDateTime()).isCloseTo(LocalDateTime.now(), within(10, SECONDS));
  }

  @Test
  void getText_WhenPlainTextMessagePartProvided() {
    // Given
    var message = buildMessage();
    var gmailMessage = new ATrackerGmailMessage(message);

    // When
    var text = gmailMessage.getText();

    // Then
    assertThat(text).isEqualTo(PLAIN_TEXT_EMAIL_BODY);
  }

  @Test
  void getText_WhenNoPlainTextMessagePartProvided_ThenDefaultsToPayloadBody() {
    // Given
    var message = buildMessage();
    var payload = message.getPayload();
    payload.getParts().clear();
    payload.setBody(createMessageBody(PAYLOD_EMAIL_BODY));
    var gmailMessage = new ATrackerGmailMessage(message);

    // When
    var text = gmailMessage.getText();

    // Then
    assertThat(text).isEqualTo(PAYLOD_EMAIL_BODY);
  }

  @Test
  void getText_WhenNoPlainTextMessagePartProvided_AndNoPayloadBody_ThenDefaultsToSnippet() {
    // Given
    var message = buildMessage();
    message.getPayload().getParts().clear();
    message.setSnippet(SNIPPET_EMAIL_BODY);
    var gmailMessage = new ATrackerGmailMessage(message);

    // When
    var text = gmailMessage.getText();

    // Then
    assertThat(text).isEqualTo(SNIPPET_EMAIL_BODY);
  }

  @Test
  void getText_WhenPlainTextMessagePartProvided_ButNoTextBody_ThenDefaultsToSnippet() {
    // Given
    var message = buildMessage();
    message.getPayload().getParts().getFirst().setBody(null);
    message.setSnippet(SNIPPET_EMAIL_BODY);
    var gmailMessage = new ATrackerGmailMessage(message);

    // When
    var text = gmailMessage.getText();

    // Then
    assertThat(text).isEqualTo(SNIPPET_EMAIL_BODY);
  }


  @Test
  void getAttachments_WhenExists() {
    // Given
    var message = buildMessage();
    var gmailMessage = new ATrackerGmailMessage(message);

    // When
    var attachments = gmailMessage.getAttachments();

    // Then
    assertThat(attachments).hasSize(1);
    var attachment = attachments.getFirst();

    assertThat(attachment.getId()).isEqualTo(ATTACHMENT_ID);
    assertThat(attachment.getFileId()).isEqualTo(PART_ID);
    assertThat(attachment.getFileName()).isEqualTo(FILE_NAME);
    assertThat(attachment.getMimeType()).isEqualTo(GmailMimeType.TEXT_CSV.getMimeType().toString());
  }

  @Test
  void getAttachments_WhenDoesNotExist() {
    // Given
    var message = buildMessage();
    message.getPayload().getParts().clear();
    var gmailMessage = new ATrackerGmailMessage(message);

    // When
    var attachments = gmailMessage.getAttachments();

    // Then
    assertThat(attachments).isEmpty();
  }
}
