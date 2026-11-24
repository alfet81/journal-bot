package com.alfet.journalbot.message.mapper;

import module java.base;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.ATTACHMENT_ID;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.CSV_FILE_NAME;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_RECIPIENT;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SENDER_STRIPPED;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SUBJECT;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EXPECTED_DATE_TIME;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.FILE_ID;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.IMG_FILE_NAME;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.createCsvAttachmentMessagePart;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.createMessage;
import static com.alfet.journalbot.message.type.MessageType.A_TRACKER;
import static com.alfet.journalbot.message.type.MessageType.LOG;
import static org.assertj.core.api.Assertions.assertThat;

import com.alfet.journalbot.atracker.message.gmail.ATrackerGmailMessage;
import com.alfet.journalbot.google.api.types.GmailMimeType;
import com.alfet.journalbot.log.message.gmail.LogGmailMessage;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import org.junit.jupiter.api.Test;

class EmailMessageMapperTest {

  private final EmailMessageMapper emailMessageMapper = new EmailMessageMapperImpl(
      new EmailMessageMetadataMapperImpl(), new EmailMessageAttachmentMapperImpl());

  @Test
  void mapEmailMessage_WhenATrackerMessageAsInput() {
    // Given
    var message = createAtrackerGmailMessage();
    var gmailMessage = new ATrackerGmailMessage(message);

    // When
    var emailMessage = emailMessageMapper.mapEmailMessage(gmailMessage);

    // Then
    assertThat(emailMessage).isNotNull();
    assertThat(emailMessage.getType()).isEqualTo(A_TRACKER);
    assertThat(emailMessage.getMetadata()).isNotNull();
    assertThat(emailMessage.getAttachments()).isNotNull().hasSize(1);

    var metadata = emailMessage.getMetadata();
    var attachment = emailMessage.getAttachments().getFirst();

    assertThat(metadata.getSender()).isEqualTo(EMAIL_SENDER_STRIPPED);
    assertThat(metadata.getRecipient()).isEqualTo(EMAIL_RECIPIENT);
    assertThat(metadata.getSubject()).isEqualTo(EMAIL_SUBJECT);
    assertThat(metadata.getDateTime()).isEqualTo(EXPECTED_DATE_TIME);

    assertThat(attachment.getId()).isEqualTo(ATTACHMENT_ID);
    assertThat(attachment.getFileId()).isEqualTo(FILE_ID);
    assertThat(attachment.getFileName()).isEqualTo(CSV_FILE_NAME);
    assertThat(attachment.getContent()).isNull();
    assertThat(attachment.getFilePath()).isNull();
    assertThat(attachment.getFullFileName()).isEqualTo(CSV_FILE_NAME);
  }

  @Test
  void mapEmailMessage_WhenLogMessageAsInput() {
    // Given
    var message = createLogGmailMessage();
    var gmailMessage = new LogGmailMessage(message);

    // When
    var emailMessage = emailMessageMapper.mapEmailMessage(gmailMessage);

    // Then
    assertThat(emailMessage).isNotNull();
    assertThat(emailMessage.getType()).isEqualTo(LOG);
    assertThat(emailMessage.getMetadata()).isNotNull();
    assertThat(emailMessage.getAttachments()).isNotNull().hasSize(1);

    var metadata = emailMessage.getMetadata();
    var attachment = emailMessage.getAttachments().getFirst();

    assertThat(metadata.getSender()).isEqualTo(EMAIL_SENDER_STRIPPED);
    assertThat(metadata.getRecipient()).isEqualTo(EMAIL_RECIPIENT);
    assertThat(metadata.getSubject()).isEqualTo(EMAIL_SUBJECT);
    assertThat(metadata.getDateTime()).isEqualTo(EXPECTED_DATE_TIME);

    assertThat(attachment.getId()).isEqualTo(ATTACHMENT_ID);
    assertThat(attachment.getFileId()).isEqualTo(FILE_ID);
    assertThat(attachment.getFileName()).isEqualTo(IMG_FILE_NAME);
    assertThat(attachment.getContent()).isNull();
    assertThat(attachment.getFilePath()).isNull();
    assertThat(attachment.getFullFileName()).isEqualTo(IMG_FILE_NAME);
  }

  @Test
  void mapEmailMessage_WhenNullInput() {
    // When
    var result = emailMessageMapper.mapEmailMessage(null);

    // Then
    assertThat(result).isNull();
  }

  private static Message createAtrackerGmailMessage() {
    var message = createMessage();
    var payload = message.getPayload();

    var mixedMultipart = new MessagePart();
    mixedMultipart.setMimeType(GmailMimeType.MULTIPART_MIXED.getMimeType().toString());
    mixedMultipart.setParts(List.of(createCsvAttachmentMessagePart()));
    payload.setParts(List.of(mixedMultipart));

    return message;
  }

  private static Message createLogGmailMessage() {
    var message = createMessage();
    var payload = message.getPayload();

    var jpegImagePart = new MessagePart();
    jpegImagePart.setMimeType(GmailMimeType.IMAGE_JPEG.getMimeType().toString());
    jpegImagePart.setPartId(FILE_ID);
    jpegImagePart.setBody(new MessagePartBody().setAttachmentId(ATTACHMENT_ID));
    jpegImagePart.setFilename(IMG_FILE_NAME);
    payload.setParts(List.of(jpegImagePart));

    return message;
  }
}
