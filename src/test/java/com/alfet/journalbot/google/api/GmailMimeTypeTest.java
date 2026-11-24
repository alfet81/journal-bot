package com.alfet.journalbot.google.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.alfet.journalbot.google.api.types.GmailMimeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class GmailMimeTypeTest {

  @ParameterizedTest(name = "[{index}] => {0}")
  @EnumSource(GmailMimeType.class)
  @DisplayName("toString")
  void toString(GmailMimeType gmailMimeType) {
    assertThat(gmailMimeType.toString()).isEqualTo(gmailMimeType.getMimeType().toString());
  }
}
