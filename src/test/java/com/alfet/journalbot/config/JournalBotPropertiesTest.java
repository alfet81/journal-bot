package com.alfet.journalbot.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig // Sets up a minimal Spring test context
@EnableConfigurationProperties(JournalBotProperties.class) // Loads only this properties bean
@TestPropertySource(properties = {
    "journal-bot.attachments-root-dir=/test/20261026"
})
class JournalBotPropertiesTest {

  private static final String ATTACHMENT_ROOT_DIR = "/test/20261026";

  @Autowired
  private JournalBotProperties journalBotProperties;

  @Test
  void testJournalBotProperties() {
    assertThat(journalBotProperties.getAttachmentsRootDir()).isEqualTo(ATTACHMENT_ROOT_DIR);
  }
}
