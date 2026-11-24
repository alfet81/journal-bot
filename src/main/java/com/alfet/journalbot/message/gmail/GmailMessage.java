package com.alfet.journalbot.message.gmail;

import com.alfet.journalbot.message.type.MessageType;

import java.util.List;

public interface GmailMessage {

  String getId();

  MessageType getType();

  GmailMessageMetadata getMetadata();

  String getText();

  List<GmailMessageAttachment> getAttachments();
}
