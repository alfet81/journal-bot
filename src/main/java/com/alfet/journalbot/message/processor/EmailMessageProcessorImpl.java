package com.alfet.journalbot.message.processor;

import com.alfet.journalbot.atracker.message.processor.ATrackerEmailMessageProcessor;
import com.alfet.journalbot.log.message.processor.LogEmailMessageProcessor;
import com.alfet.journalbot.message.domain.EmailMessage;
import com.alfet.journalbot.message.type.MessageType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component("emailMessageProcessor")
public class EmailMessageProcessorImpl implements EmailMessageProcessor {

  private final LogEmailMessageProcessor logEmailMessageProcessor;
  private final ATrackerEmailMessageProcessor atrackerEmailMessageProcessor;

  @Override
  public void process(@NonNull EmailMessage emailMessage) {
    getEmailMessageProcessor(emailMessage.getType()).process(emailMessage);
  }

  private EmailMessageProcessor getEmailMessageProcessor(@NonNull MessageType messageType) {
    return switch (messageType) {
      case LOG -> logEmailMessageProcessor;
      case A_TRACKER -> atrackerEmailMessageProcessor;
    };
  }
}
