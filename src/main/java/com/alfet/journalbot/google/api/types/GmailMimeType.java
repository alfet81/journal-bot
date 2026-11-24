package com.alfet.journalbot.google.api.types;

import lombok.Getter;
import org.springframework.util.MimeType;

@Getter
public enum GmailMimeType {
  MULTIPART_MIXED("multipart/mixed"),
  MULTIPART_ALTERNATIVE("multipart/alternative"),
  TEXT_PLAIN("text/plain"),
  TEXT_CSV("text/csv"),
  APPLICATION_PDF("application/pdf"),
  IMAGE_JPEG("image/jpeg");

  GmailMimeType(String mimeTypeValue) {
    this.mimeType = MimeType.valueOf(mimeTypeValue);
  }

  private final MimeType mimeType;

  @Override
  public String toString() {
    return mimeType.toString();
  }
}
