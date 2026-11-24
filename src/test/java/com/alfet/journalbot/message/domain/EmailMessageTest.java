package com.alfet.journalbot.message.domain;

import module java.base;
import static com.alfet.journalbot.message.type.MessageType.LOG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.alfet.journalbot.message.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EmailMessageTest {

  private static final String EMAIL_SENDER = "john.doe@gmail.com";
  private static final String EMAIL_RECIPIENT = "journal.bot@gmail.com";
  private static final String EMAIL_SUBJECT = "log 102626";
  private static final String FILE_NAME = "CSVFile.csv";
  private static final LocalDateTime EMAIL_DATE_TIME = LocalDateTime.of(2025, 10, 4, 21, 43, 57);
  private static final String ATTACHMENT_ID = "XYZ";
  private static final String EMAIL_TEXT = "Some text";
  private static final String MESSAGE_ID = "1a1a02b0b9ad1b53";
  private static final String FILE_ID = "ABC";

  private static Stream<Arguments> provideBuilderInvalidParametersTestArgs() {
    var metadata = EmailMessageMetadata.builder()
        .sender(EMAIL_SENDER)
        .recipient(EMAIL_RECIPIENT)
        .subject(EMAIL_SUBJECT)
        .dateTime(EMAIL_DATE_TIME)
        .build();
    return Stream.of(
      arguments("id is null", null, LOG, metadata, EMAIL_TEXT, "id is marked non-null but is null"),
      arguments("type is null", MESSAGE_ID, null, metadata, EMAIL_TEXT,
          "type is marked non-null but is null"),
      arguments("metadata is null", MESSAGE_ID, LOG, null, EMAIL_TEXT,
          "metadata is marked non-null but is null")
    );
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideBuilderInvalidParametersTestArgs")
  @DisplayName("builder_WhenInvalidArguments")
  void builder_WhenInvalidArguments(String scenario, String id, MessageType type,
      EmailMessageMetadata metadata, String text, String expectedErrorMessage) {
    // When
    var exception = catchException(() -> EmailMessage.builder()
        .id(id)
        .type(type)
        .metadata(metadata)
        .build());

    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(expectedErrorMessage);
  }

  @Test
  void withAttachments_WhenNonNull() {
    // Given
    var emailMessage = buildEmailMessage();

    // When
    var result = emailMessage.withAttachments(List.of(buildEmailMessageAttachment()));

    // Then
    assertThat(result.getAttachments()).hasSize(1);
  }

  @Test
  void withAttachments_WhenNull() {
    // Given
    var emailMessage = buildEmailMessage();

    // When
    var result = emailMessage.withAttachments(null);

    // Then
    assertThat(result.getAttachments()).isEmpty();
  }

  private static EmailMessage buildEmailMessage() {
    var metadata = EmailMessageMetadata.builder()
        .recipient(EMAIL_RECIPIENT)
        .sender(EMAIL_SENDER)
        .subject(EMAIL_SUBJECT)
        .dateTime(EMAIL_DATE_TIME)
        .build();
    return EmailMessage.builder()
        .id(MESSAGE_ID)
        .type(LOG)
        .text(EMAIL_TEXT)
        .metadata(metadata)
        .build();
  }

  private static EmailMessageAttachment buildEmailMessageAttachment() {
    return EmailMessageAttachment.builder()
        .id(ATTACHMENT_ID)
        .fileName(FILE_NAME)
        .fileId(FILE_ID)
        .build();
  }
}
