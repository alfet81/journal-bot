package com.alfet.journalbot.message.provider;

import module java.base;

import com.alfet.journalbot.message.domain.EmailMessage;

/**
 * Interface for email message extractors.
 */
public interface EmailMessageProvider {

  List<EmailMessage> getEmailMessages();
}
