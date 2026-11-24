package com.alfet.journalbot.message.gmail;

import static com.alfet.journalbot.google.api.types.GmailMimeType.IMAGE_JPEG;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.ATTACHMENT_ID;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.CSV_FILE_NAME;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.FILE_ID;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.createMessage;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import org.junit.jupiter.api.Test;

import java.util.List;

class GmailMessageAttachmentImplTest {

  @Test
  void testGmailMessageAttachment() {
    // Given
    var message = createMessage();
    createAttachment(message);


    // When
    var gmailMessageAttachment = new GmailMessageAttachmentImpl(
        message.getPayload().getParts().getFirst());

    // Then
    assertThat(gmailMessageAttachment.getId()).isEqualTo(ATTACHMENT_ID);
    assertThat(gmailMessageAttachment.getFileId()).isEqualTo(FILE_ID);
    assertThat(gmailMessageAttachment.getFileName()).isEqualTo(CSV_FILE_NAME);
    assertThat(gmailMessageAttachment.getMimeType()).isEqualTo(IMAGE_JPEG.toString());
  }

  private static void createAttachment(Message message) {
    var messagePart = new MessagePart();
    messagePart.setMimeType(IMAGE_JPEG.toString());
    messagePart.setBody(new MessagePartBody().setAttachmentId(ATTACHMENT_ID));
    messagePart.setFilename(CSV_FILE_NAME);
    messagePart.setPartId(FILE_ID);
    message.getPayload().setParts(List.of(messagePart));
  }
}
