package com.alfet.journalbot.message.gmail;

import module java.base;

public interface GmailMessageMetadata {

  String getRecipient();

  String getSender();

  String getSubject();

  LocalDateTime getDateTime();
}
