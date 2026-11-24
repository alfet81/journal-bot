package com.alfet.journalbot.config;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig // Sets up a minimal Spring test context
@EnableConfigurationProperties(GoogleApiProperties.class) // Loads only this properties bean
@TestPropertySource(properties = {
    "google-api.app-name=journal-bot",
    "google-api.user-id=journal.bot@gmail.com",
    "google-api.token-dir: GoogleApi/tokens",
    "google-api.credentials-file: /credentials.json"
})
class GoogleApiPropertiesTest {
  @Autowired
  private GoogleApiProperties googleApiProperties;

  @Test
  void testGoogleApiProperties() {
    assertThat(googleApiProperties.appName()).isEqualTo("journal-bot");
    assertThat(googleApiProperties.userId()).isEqualTo("journal.bot@gmail.com");
    assertThat(googleApiProperties.tokenDir()).isEqualTo("GoogleApi/tokens");
    assertThat(googleApiProperties.credentialsFile()).isEqualTo("/credentials.json");
  }
}
