package com.alfet.journalbot.message.processor;

import com.alfet.journalbot.atracker.message.processor.ATrackerEmailMessageProcessor;
import com.alfet.journalbot.log.message.processor.LogEmailMessageProcessor;
import com.alfet.journalbot.message.domain.EmailMessage;
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
    switch (emailMessage.getType()) {
      case LOG -> logEmailMessageProcessor.process(emailMessage);
      case A_TRACKER -> atrackerEmailMessageProcessor.process(emailMessage);
      default -> throw new IllegalStateException("Unrecognized or unsupported message type: "
          + emailMessage.getType());
    }
  }
}
