package com.alfet.journalbot.message.mapper;

import static com.alfet.journalbot.helper.EmailMessageTestHelper.ATTACHMENT_ID;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.CSV_FILE_NAME;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.FILE_ID;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.createCsvAttachmentMessagePart;
import static org.assertj.core.api.Assertions.assertThat;

import com.alfet.journalbot.message.gmail.GmailMessageAttachmentImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

class EmailMessageAttachmentMapperTest {

  private final EmailMessageAttachmentMapper emailMessageAttachmentMapper
      = new EmailMessageAttachmentMapperImpl();

  @Test
  void mapEmailMessageAttachment_WhenValidData() {
    // Given
    var messagePart = createCsvAttachmentMessagePart();
    var gmailMessageAttachment = new GmailMessageAttachmentImpl(messagePart);

    // When
    var result = emailMessageAttachmentMapper.mapEmailMessageAttachment(gmailMessageAttachment);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getFileName()).isEqualTo(CSV_FILE_NAME);
    assertThat(result.getId()).isEqualTo(ATTACHMENT_ID);
    assertThat(result.getFileId()).isEqualTo(FILE_ID);
    assertThat(result.getContent()).isNull();
  }

  @Test
  void mapEmailMessageAttachment_WhenNullInput() {
    // When
    var result = emailMessageAttachmentMapper.mapEmailMessageAttachment(null);

    // Then
    assertThat(result).isNull();
  }

  @Test
  void mapEmailMessageAttachments_WhenValidData() {
    // Given
    var messagePart = createCsvAttachmentMessagePart();
    var gmailMessageAttachment = new GmailMessageAttachmentImpl(messagePart);

    // When
    var result = emailMessageAttachmentMapper.mapEmailMessageAttachments(
        List.of(gmailMessageAttachment));

    // Then
    assertThat(result).isNotNull().hasSize(1);
    var gmailMessageAttachmentResult = result.getFirst();
    assertThat(gmailMessageAttachmentResult.getFileName()).isEqualTo(CSV_FILE_NAME);
    assertThat(gmailMessageAttachmentResult.getId()).isEqualTo(ATTACHMENT_ID);
    assertThat(gmailMessageAttachmentResult.getFileId()).isEqualTo(FILE_ID);
    assertThat(gmailMessageAttachmentResult.getContent()).isNull();
  }

  @Test
  void mapEmailMessageAttachments_WhenNullInput() {
    // When
    var result = emailMessageAttachmentMapper.mapEmailMessageAttachments(null);

    // Then
    assertThat(result).isNull();
  }
}
