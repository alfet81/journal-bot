package com.alfet.journalbot.util;

import module java.base;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MiscUtils {

  private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
      DateTimeFormatter.ofPattern("MMddyyyy"),
      DateTimeFormatter.ofPattern("MMddyy"),
      DateTimeFormatter.ofPattern("MMM d, yyyy")
  );

  public static Optional<LocalDate> parseDate(String value) {
    if (StringUtils.isEmpty(value)) {
      return Optional.empty();
    }

    if (value.length() == 4) {
      value += LocalDate.now().getYear();
    }

    for (var formatter : DATE_FORMATTERS) {
      try {
        var date = LocalDate.parse(value, formatter);
        logger.atDebug().log("Successfully parsed date '{}' from string value '{}'", date, value);
        return Optional.of(date);
      } catch (DateTimeParseException _) {
      }
    }
    logger.atDebug().log("Failed to parse date from string value '{}'", value);
    return Optional.empty();
  }

  public static String formatDate(LocalDate date) {
    return DATE_FORMATTERS.get(1).format(date);
  }

  public static String get5YearRange(int year) {
    var yearAsString = String.valueOf(year);
    int lastDigit = toInt(StringUtils.right(yearAsString, 1));
    return YearRange.calculate(year, lastDigit).toString();
  }

  public static <T> Optional<T> asOptional(T value) {
    return Optional.ofNullable(value);
  }


  public static Optional<String> asOptional(String value) {
    return Optional.ofNullable(trimToNull(value));
  }

  private record YearRange(int start, int end) {

    static YearRange calculate(int year, int lastDigit) {
      if (lastDigit == 0) {
        return new YearRange(year - 4, year);
      }
      int startDigit = (lastDigit <= 5 ? 1 : 6);
      int decade = year - lastDigit;
      int start = decade + startDigit;
      int end = start + 4;
      return new YearRange(start, end);
    }

    @Override
    public String toString() {
      return start + " - " + end;
    }
  }
}
