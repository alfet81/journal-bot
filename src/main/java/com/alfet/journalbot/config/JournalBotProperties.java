package com.alfet.journalbot.config;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Value
@ConfigurationProperties(prefix = "journal-bot")
public class JournalBotProperties {

  String attachmentsRootDir;
  GmailConfig gmailConfig;

  @Value
  public static class GmailConfig {

    String atrackerQuery;
    String logQuery;
  }
}
