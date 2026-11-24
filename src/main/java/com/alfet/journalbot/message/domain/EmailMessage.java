package com.alfet.journalbot.message.domain;

import module java.base;

import com.alfet.journalbot.message.type.MessageType;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.With;

@Value
@Builder
@ToString
public class EmailMessage {

  @NonNull
  String id;

  @NonNull
  MessageType type;

  @NonNull
  EmailMessageMetadata metadata;

  @With
  @ToString.Exclude
  String text;

  List<EmailMessageAttachment> attachments;

  public LocalDateTime getDateTime() {
    return metadata.getDateTime();
  }

  public EmailMessage withAttachments(List<EmailMessageAttachment> attachments) {
    return EmailMessage.builder()
        .id(id)
        .type(type)
        .metadata(metadata)
        .text(text)
        .attachments(attachments != null ? List.copyOf(attachments) : List.of())
        .build();
  }
}
