package com.alfet.journalbot.message.gmail;

import com.google.api.services.gmail.model.MessagePart;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GmailMessageAttachmentImpl implements GmailMessageAttachment {

  private final MessagePart messagePart;

  @Override
  public String getId() {
    return messagePart.getBody().getAttachmentId();
  }

  @Override
  public String getFileId() {
    return messagePart.getPartId();
  }

  @Override
  public String getFileName() {
    return messagePart.getFilename();
  }

  @Override
  public String getMimeType() {
    return messagePart.getMimeType();
  }
}
