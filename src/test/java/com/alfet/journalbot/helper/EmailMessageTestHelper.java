package com.alfet.journalbot.helper;

import module java.base;

import com.alfet.journalbot.google.api.types.GmailMimeType;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;

public class EmailMessageTestHelper {

  public static final String EMAIL_SENDER_ORIGINAL = "John Doe <john.doe@gmail.com>";
  public static final String EMAIL_SENDER_STRIPPED = "john.doe@gmail.com";
  public static final String EMAIL_RECIPIENT = "journal.bot@gmail.com";
  public static final String EMAIL_SUBJECT = "Task Detail (Oct 4, 2025)";
  public static final String EMAIL_DATE_TIME = "Sat, 4 Oct 2025 21:43:57 -0400";
  public static final String ATTACHMENT_ID = "ABC";
  public static final String CSV_FILE_NAME = "CSVFile.csv";
  public static final String IMG_FILE_NAME = "IMGFile.jpg";
  public static final String FILE_ID = "XYZ";
  public static final LocalDateTime EXPECTED_DATE_TIME = LocalDateTime.of(2025, 10, 4, 21, 43, 57);
  public static final String MESSAGE_ID = "1a1a02b0b9ad1b53";

  public static MessagePart createCsvAttachmentMessagePart() {
    var messagePartBody = new MessagePartBody();
    messagePartBody.setAttachmentId(ATTACHMENT_ID);

    var messagePart = new MessagePart();
    messagePart.setFilename(CSV_FILE_NAME);
    messagePart.setBody(messagePartBody);
    messagePart.setPartId(FILE_ID);
    messagePart.setMimeType(GmailMimeType.TEXT_CSV.getMimeType().toString());
    return messagePart;
  }

  public static MessagePart createMessagePayload() {
    var payload = new MessagePart();
    payload.setHeaders(createMessagePartHeaders());
    return payload;
  }

  public static List<MessagePartHeader> createMessagePartHeaders() {
    var toHeader = createMessagePartHeader("TO", EMAIL_RECIPIENT);
    var fromHeader = createMessagePartHeader("FROM", EMAIL_SENDER_ORIGINAL);
    var subject = createMessagePartHeader("SUBJECT", EMAIL_SUBJECT);
    var date = createMessagePartHeader("DATE", EMAIL_DATE_TIME);
    return List.of(toHeader, fromHeader, subject, date);
  }

  public static MessagePartHeader createMessagePartHeader(String name, String value) {
    return new MessagePartHeader().setName(name).setValue(value);
  }

  public static Message createMessage() {
    var message = new Message().setId(MESSAGE_ID);
    var payload = createMessagePayload();
    message.setPayload(payload);
    return message;
  }
}
