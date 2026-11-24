package com.alfet.journalbot.google.api.types;

import lombok.Getter;
import org.springframework.util.MimeType;

@Getter
public enum GoogleDriveMimeType {
  DOCUMENT("vnd.google-apps.document"),
  FILE("vnd.google-apps.file"),
  FOLDER("vnd.google-apps.folder");

  GoogleDriveMimeType(String mimeTypeValue) {
    this.mimeType = MimeType.valueOf("application/" + mimeTypeValue);
  }

  private final MimeType mimeType;
}
