package com.alfet.journalbot.service;

import module java.base;

import static java.util.Comparator.comparing;

import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.message.domain.EmailMessage;
import com.alfet.journalbot.message.processor.EmailMessageProcessor;
import com.alfet.journalbot.message.provider.EmailMessageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service that fetches emails via EmailMessageExtractor implementations and processes them.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JournalBotService {

  private final List<EmailMessageProvider> emailMessageProviders;

  @Qualifier("emailMessageProcessor")
  private final EmailMessageProcessor emailMessageProcessor;

  public void runProcess() {
    getEmailMessages().forEach(this::processEmailMessage);
  }

  private List<EmailMessage> getEmailMessages() {
    return emailMessageProviders.stream()
        .map(EmailMessageProvider::getEmailMessages)
        .flatMap(List::stream)
        .sorted(comparing(EmailMessage::getDateTime))
        .toList();
  }

  private void processEmailMessage(EmailMessage message) {
    try {
      logger.atDebug().log("Processing: {}", message);
      emailMessageProcessor.process(message);
    } catch (ApplicationException e) {
      logger.atError().setCause(e).log("Failed to process email message: {}.", message);
    }
  }
}
