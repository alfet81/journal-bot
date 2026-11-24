package com.alfet.journalbot.google.api;

import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.DOCUMENT;
import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.FILE;
import static com.alfet.journalbot.google.api.types.GoogleDriveMimeType.FOLDER;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GoogleDriveMimeTypeTest {

  @Test
  void getMimeType() {
    assertThat(DOCUMENT.getMimeType()).hasToString("application/vnd.google-apps.document");
    assertThat(FILE.getMimeType()).hasToString("application/vnd.google-apps.file");
    assertThat(FOLDER.getMimeType()).hasToString("application/vnd.google-apps.folder");
  }
}
