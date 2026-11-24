package com.alfet.journalbot.message.mapper;

import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_RECIPIENT;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SENDER_STRIPPED;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SUBJECT;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EXPECTED_DATE_TIME;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.createMessagePayload;
import static org.assertj.core.api.Assertions.assertThat;

import com.alfet.journalbot.message.gmail.GmailMessageMetadata;
import com.alfet.journalbot.message.gmail.GmailMessageMetadataImpl;
import org.junit.jupiter.api.Test;

class EmailMessageMetadataMapperTest {

  private final EmailMessageMetadataMapper emailMessageMetadataMapper =
      new EmailMessageMetadataMapperImpl();

  @Test
  void mapEmailMessageMetadata_WhenValidData() {
    // Given
    var payload = createMessagePayload();
    GmailMessageMetadata gmailMessageMetadata = new GmailMessageMetadataImpl(payload);

    // When
    var result = emailMessageMetadataMapper.mapEmailMessageMetadata(gmailMessageMetadata);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getSender()).isEqualTo(EMAIL_SENDER_STRIPPED);
    assertThat(result.getRecipient()).isEqualTo(EMAIL_RECIPIENT);
    assertThat(result.getSubject()).isEqualTo(EMAIL_SUBJECT);
    assertThat(result.getDateTime()).isEqualTo(EXPECTED_DATE_TIME);
  }

  @Test
  void mapEmailMessageMetadata_WhenNullInput() {
    // When
    var result = emailMessageMetadataMapper.mapEmailMessageMetadata(null);

    // Then
    assertThat(result).isNull();
  }
}
