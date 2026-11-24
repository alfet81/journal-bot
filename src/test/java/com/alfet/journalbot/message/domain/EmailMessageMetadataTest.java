package com.alfet.journalbot.message.domain;

import module java.base;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_RECIPIENT;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SENDER_STRIPPED;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SUBJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EmailMessageMetadataTest {

  private static Stream<Arguments> provideBuilderInvalidTestArgs() {
    var dateTime = LocalDateTime.now();
    return Stream.of(
        arguments("sender is null", null, EMAIL_RECIPIENT, EMAIL_SUBJECT, dateTime,
            "sender is marked non-null but is null"),
        arguments("recipient is null", EMAIL_SENDER_STRIPPED, null, EMAIL_SUBJECT, dateTime,
            "recipient is marked non-null but is null"),
        arguments("subject is null", EMAIL_SENDER_STRIPPED, EMAIL_RECIPIENT, null, dateTime,
            "subject is marked non-null but is null"),
        arguments("dateTime is null", EMAIL_SENDER_STRIPPED, EMAIL_RECIPIENT, EMAIL_SUBJECT, null,
            "dateTime is marked non-null but is null")
    );
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideBuilderInvalidTestArgs")
  @DisplayName("builder_WhenInvalidArguments")
  void builder_WhenInvalidArguments(String scenario, String sender, String recipient,
      String subject, LocalDateTime dateTime, String expectedMessage) {
    // When
    var exception = catchException(() -> EmailMessageMetadata.builder()
        .sender(sender)
        .recipient(recipient)
        .subject(subject)
        .dateTime(dateTime)
        .build());

    // Then
    assertThat(exception).isInstanceOf(IllegalArgumentException.class).hasMessage(expectedMessage);
  }
}
