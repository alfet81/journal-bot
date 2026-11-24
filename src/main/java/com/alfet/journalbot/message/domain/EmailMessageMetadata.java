package com.alfet.journalbot.message.domain;

import module java.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailMessageMetadata {

  @NonNull
  String recipient;

  @NonNull
  String sender;

  @NonNull
  String subject;

  @NonNull
  LocalDateTime dateTime;
}
