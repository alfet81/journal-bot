package com.alfet.journalbot.util;

import module java.base;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeUtils {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMM d, yyyy");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm:ssa");
  private static final Pattern DATE_TIME_REGEX_PATTERN = Pattern.compile(
      "^([\\w\\s\\p{Punct}]+) at ([\\w\\s\\p{Punct}]+)$");

  public static Optional<LocalDateTime> parse(String dateTime) {

    if (StringUtils.isEmpty(dateTime)) {
      return Optional.empty();
    }

    dateTime = cleanup(dateTime);

    var matcher = DATE_TIME_REGEX_PATTERN.matcher(dateTime);

    if (matcher.find()) {
      try {
        var dateString = StringUtils.trim(matcher.group(1));
        var timeString = StringUtils.trim(matcher.group(2));
        var date = LocalDate.parse(dateString, DATE_FORMATTER);
        var time = LocalTime.parse(timeString, TIME_FORMATTER);
        return Optional.of(LocalDateTime.of(date, time));
      } catch (DateTimeParseException e) {
        logger.atWarn().log("Failed to parse date/time: {}", e.getMessage());
      }
    }
    return Optional.empty();
  }

  static String cleanup(String originalValue) {
    return originalValue.replaceAll("\\P{ASCII}", EMPTY);
  }
}
