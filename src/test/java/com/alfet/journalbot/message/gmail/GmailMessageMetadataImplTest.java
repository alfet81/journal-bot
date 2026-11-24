package com.alfet.journalbot.message.gmail;

import module java.base;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_DATE_TIME;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_RECIPIENT;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SENDER_ORIGINAL;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SENDER_STRIPPED;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SUBJECT;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EXPECTED_DATE_TIME;
import static com.alfet.journalbot.message.gmail.GmailHeader.DATE;
import static com.alfet.journalbot.message.gmail.GmailHeader.FROM;
import static com.alfet.journalbot.message.gmail.GmailHeader.SUBJECT;
import static com.alfet.journalbot.message.gmail.GmailHeader.TO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchException;
import static org.assertj.core.api.Assertions.within;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.message.gmail.GmailMessageMetadataImpl.GmailHeaderRecord;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.junit.jupiter.api.Test;

class GmailMessageMetadataImplTest {

  @Test
  void constructor_WhenNullInputArgument() {
    assertThatThrownBy(() -> new GmailMessageMetadataImpl(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("messagePayload is marked non-null but is null");
  }

  @Test
  void constructor_WhenNoHeadersFound() {
    // When
    var exception = catchException(() -> new GmailMessageMetadataImpl(new MessagePart()));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasMessage("Gmail message headers are missing.");
  }


  @Test
  void constructor_WhenDuplicatedHeadersFound() {
    // Given
    var payload = new MessagePart();
    payload.setHeaders(buildHeaders());

    // When
    var metadata = new GmailMessageMetadataImpl(payload);

    // Then
    assertThat(metadata.getSender()).isEqualTo(EMAIL_SENDER_STRIPPED);
    assertThat(metadata.getRecipient()).isEqualTo(EMAIL_RECIPIENT);
    assertThat(metadata.getSubject()).isEqualTo(EMAIL_SUBJECT);
    assertThat(metadata.getDateTime()).isEqualTo(EXPECTED_DATE_TIME);
  }

  @Test
  void constructor_WhenDateTimeParseExceptionThrown() {
    // Given
    var payload = new MessagePart();
    payload.setHeaders(buildHeadersWithInvalidDateTime());

    // When
    var metadata = new GmailMessageMetadataImpl(payload);

    // Then
    assertThat(metadata.getSender()).isEqualTo(EMAIL_SENDER_STRIPPED);
    assertThat(metadata.getRecipient()).isEqualTo(EMAIL_RECIPIENT);
    assertThat(metadata.getSubject()).isEqualTo(EMAIL_SUBJECT);
    assertThat(metadata.getDateTime()).isCloseTo(LocalDateTime.now(), within(5, SECONDS));
  }

  @Test
  void testGmailHeaderRecord_parse_WhenUnsupportedHeadersFound() {
    // Given
    var unsupportedHeader = new MessagePartHeader().setName("BAD").setValue("TEST");

    // When
    var exception = catchException(() -> GmailHeaderRecord.parse(unsupportedHeader));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasMessage("Unsupported metadata header: BAD");
  }

  @Test
  void testGmailHeaderRecord_WhenHeaderIsNull() {
    assertThatThrownBy(() -> GmailHeaderRecord.of(null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("header is marked non-null but is null");
  }

  @Test
  void testGmailHeaderRecord_isValid_WhenHeaderValueIsNull() {
    assertThat(GmailHeaderRecord.of(TO, null).isValid()).isFalse();
  }

  private static List<MessagePartHeader> buildHeaders() {
    var toHeader1 = buildMessagePartHeader(TO.toString(), EMAIL_RECIPIENT);
    var toHeader2 = buildMessagePartHeader(TO.toString(), null);
    var fromHeader = buildMessagePartHeader(FROM.toString(), EMAIL_SENDER_ORIGINAL);
    var subjectHeader1 = buildMessagePartHeader(SUBJECT.toString(), EMAIL_SUBJECT);
    var subjectHeader2 = buildMessagePartHeader(SUBJECT.toString(), "Should be ignored");
    var dateTimeHeader1 = buildMessagePartHeader(DATE.toString(), EMAIL_DATE_TIME);
    var dateTimeHeader2 = buildMessagePartHeader(DATE.toString(), "Should be ignored");

    return List.of(toHeader1, toHeader2, fromHeader, subjectHeader1, subjectHeader2,
        dateTimeHeader1, dateTimeHeader2);
  }

  private static List<MessagePartHeader> buildHeadersWithInvalidDateTime() {
    var toHeader = buildMessagePartHeader(TO.toString(), EMAIL_RECIPIENT);
    var fromHeader = buildMessagePartHeader(FROM.toString(), EMAIL_SENDER_ORIGINAL);
    var subjectHeader = buildMessagePartHeader(SUBJECT.toString(), EMAIL_SUBJECT);
    var dateTimeHeader = buildMessagePartHeader(DATE.toString(), "Bad, 4 Oct 2025 21:43:57 -0400");

    return List.of(toHeader, fromHeader, subjectHeader, dateTimeHeader);
  }

  private static MessagePartHeader buildMessagePartHeader(String name, String value) {
    return new MessagePartHeader().setName(name).setValue(value);
  }
}
