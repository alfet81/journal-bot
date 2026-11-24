package com.alfet.journalbot.message.gmail;

import module java.base;
import static com.alfet.journalbot.message.gmail.GmailHeader.DATE;
import static com.alfet.journalbot.message.gmail.GmailHeader.FROM;
import static com.alfet.journalbot.message.gmail.GmailHeader.SUBJECT;
import static com.alfet.journalbot.message.gmail.GmailHeader.TO;
import static com.alfet.journalbot.util.MiscUtils.asOptional;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import static java.util.stream.Collectors.toMap;

import com.alfet.journalbot.exception.ApplicationException;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

@Slf4j
public class GmailMessageMetadataImpl implements GmailMessageMetadata {

  private static final Pattern EMAIL_ADDRESS_REGEX = Pattern.compile("^.*<(.*)>$");
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss XX");
  private static final ZoneId EDT_ZONE_ID = ZoneId.of("America/New_York");

  private final Map<GmailHeader, String> metadata;

  private static final Predicate<MessagePartHeader> IS_SUPPORTED_EMAIL_HEADER =
      (var header) -> GmailHeader.forName(header.getName()).isPresent();

  public GmailMessageMetadataImpl(@NonNull MessagePart messagePayload) {
    if (CollectionUtils.isEmpty(messagePayload.getHeaders())) {
      throw new ApplicationException("Gmail message headers are missing.");
    }
    metadata = messagePayload.getHeaders().stream()
        //.peek((var header) -> logger.trace("Header: {} = {}", header.getName(), header.getValue()))
        .filter(IS_SUPPORTED_EMAIL_HEADER)
        .map(GmailHeaderRecord::parse)
        .filter(GmailHeaderRecord::isValid)
        //.peek((var rec) -> logger.debug("Header: {} = {}", rec.header(), rec.value()))
        .collect(toMap(
            GmailHeaderRecord::header,
            GmailHeaderRecord::value,
            (k1, _) -> k1,
            () -> new EnumMap<>(GmailHeader.class)));
  }

  @Override
  public String getRecipient() {
    return getValue(TO).map(GmailMessageMetadataImpl::parseEmailAddress).orElse(EMPTY);
  }

  @Override
  public String getSender() {
    return getValue(FROM).map(GmailMessageMetadataImpl::parseEmailAddress).orElse(EMPTY);
  }

  @Override
  public String getSubject() {
    return getValue(SUBJECT).orElse(EMPTY);
  }

  @Override
  public LocalDateTime getDateTime() {
    return getValue(DATE).map(GmailMessageMetadataImpl::parseDateTime).orElse(LocalDateTime.now());
  }

  private Optional<String> getValue(GmailHeader header) {
    return asOptional(metadata.get(header));
  }

  private static String parseEmailAddress(String emailAddress) {
    var matcher = EMAIL_ADDRESS_REGEX.matcher(emailAddress);
    return matcher.find() ? matcher.group(1) : emailAddress;
  }

  private static LocalDateTime parseDateTime(String dateTime) {
    try {
      return ZonedDateTime.parse(dateTime, DATE_TIME_FORMATTER)
          .withZoneSameInstant(EDT_ZONE_ID).toLocalDateTime();
    } catch (DateTimeParseException e) {
      logger.atWarn().setCause(e).log("Failed to parse date/time: {}", dateTime);
      return null;
    }
  }

  record GmailHeaderRecord(GmailHeader header, String value) {

    static GmailHeaderRecord parse(MessagePartHeader messagePartHeader) {
      var header = GmailHeader.forName(messagePartHeader.getName())
          .orElseThrow(() -> new ApplicationException("Unsupported metadata header: "
              + messagePartHeader.getName()));
      return GmailHeaderRecord.of(header, messagePartHeader.getValue());
    }

    static GmailHeaderRecord of(@NonNull GmailHeader header, String value) {
      return new GmailHeaderRecord(header, value);
    }

    boolean isValid() {
      var valid = value != null;
      if (!valid) {
        logger.atWarn().log("The header {} value is null. Skipping.", header);
      }
      return valid;
    }
  }


}
