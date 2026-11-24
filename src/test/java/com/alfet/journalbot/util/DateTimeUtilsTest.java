package com.alfet.journalbot.util;

import module java.base;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DateTimeUtilsTest {

  private static final String INVALID_DATE_TIME_FORMAT = "Bad 5, 2025 at 12:00:00AM";

  public static Stream<Arguments> provideParseDateTimeTestArgs() {
    return Stream.of(
        arguments("Oct 5, 2025 at 12:00:00AM", LocalDateTime.of(2025, 10, 5, 0, 0)),
        arguments("Oct 15, 2025 at 12:00:00PM", LocalDateTime.of(2025, 10, 15, 12, 0)),
        arguments("Oct 25, 2025 at 1:35:00PM", LocalDateTime.of(2025, 10, 25, 13, 35))
    );
  }

  @ParameterizedTest(name = "[{index}] => {0} yields \"{1}\"")
  @MethodSource("provideParseDateTimeTestArgs")
  @DisplayName("testParse_WhenValidDateTimeString_ThenLocalDateTimeReturned")
  void parse_WhenValidDateTimeString_ThenLocalDateTimeReturned(String sourceValue,
      LocalDateTime expectedValue) {
    // When
    var actualValue = DateTimeUtils.parse(sourceValue);

    // Then
    assertThat(actualValue).isNotEmpty().hasValue(expectedValue);
  }

  @ParameterizedTest(name = "[{index}] => Value: {0} yields empty result")
  @NullSource
  @EmptySource
  @ValueSource(strings = {"INVALID", INVALID_DATE_TIME_FORMAT})
  @DisplayName("testParse_WhenInvalidDateTimeString_ThenEmptyValue")
  void parse_WhenInvalidDateTimeString_ThenEmptyValue(String sourceValue) {
    assertThatCode(() -> DateTimeUtils.parse(sourceValue)).doesNotThrowAnyException();
    assertThat(DateTimeUtils.parse(sourceValue)).isEmpty();
  }
}
