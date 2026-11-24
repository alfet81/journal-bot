package com.alfet.journalbot.message.domain;

import static com.alfet.journalbot.util.FileUtils.FOLDER_SEPARATOR;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.With;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailMessageAttachment {

  @NonNull
  String id;

  @NonNull
  String fileId;

  @NonNull
  String fileName;

  @With
  String filePath;

  @With
  @ToString.Exclude
  byte[] content;

  public String getFullFileName() {
    return filePath != null ? filePath + FOLDER_SEPARATOR + fileName : fileName;
  }
}
