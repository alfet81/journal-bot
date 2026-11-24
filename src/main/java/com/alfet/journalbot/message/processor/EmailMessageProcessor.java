package com.alfet.journalbot.message.processor;

import com.alfet.journalbot.message.domain.EmailMessage;

public interface EmailMessageProcessor {

  void process(EmailMessage emailMessage);
}
