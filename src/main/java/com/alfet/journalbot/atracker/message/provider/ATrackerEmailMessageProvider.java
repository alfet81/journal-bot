package com.alfet.journalbot.atracker.message.provider;

import module java.base;

import com.alfet.journalbot.atracker.message.gmail.ATrackerGmailMessage;
import com.alfet.journalbot.config.JournalBotProperties;
import com.alfet.journalbot.google.api.GmailService;
import com.alfet.journalbot.message.mapper.EmailMessageMapper;
import com.alfet.journalbot.message.provider.AbstractEmailMessageProvider;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Component;

@Component
public class ATrackerEmailMessageProvider
    extends AbstractEmailMessageProvider<ATrackerGmailMessage> {

  public ATrackerEmailMessageProvider(JournalBotProperties journalBotProperties,
      GmailService gmailService, EmailMessageMapper emailMessageMapper,
      Function<Message, ATrackerGmailMessage> gmailMessageFactory) {
    super(journalBotProperties, gmailService, emailMessageMapper, gmailMessageFactory);
  }

  @Override
  protected String getMessageFilterQuery() {
    return getJournalBotProperties().getGmailConfig().getAtrackerQuery();
  }
}
