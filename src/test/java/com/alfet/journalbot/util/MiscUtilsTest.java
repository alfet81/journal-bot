package com.alfet.journalbot.util;

import module java.base;
import static com.alfet.journalbot.util.MiscUtils.formatDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import static java.time.Month.APRIL;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class MiscUtilsTest {

  private static Stream<Arguments> provideParseDateTestArgs() {
    return Stream.of(
        arguments("04012024", LocalDate.of(2024, APRIL, 1)),
        arguments("040224", LocalDate.of(2024, APRIL, 2)),
        arguments("0403", LocalDate.of(LocalDate.now().getYear(), APRIL, 3)),
        arguments("Apr 3, 2025", LocalDate.of(2025, APRIL, 3)),
        arguments("Apr 13, 2025", LocalDate.of(2025, APRIL, 13))
    );
  }

  @ParameterizedTest(name = "[{index}] => \"{0}\" yields \"{1}\"")
  @MethodSource("provideParseDateTestArgs")
  @DisplayName("testParseDateWithValidInput")
  void testParseDate_WithValidInput(String inputValue, LocalDate expectedResult) {
    var actualValue = MiscUtils.parseDate(inputValue);
    assertThat(actualValue).isNotEmpty().hasValue(expectedResult);
  }

  @Test
  void testParseDateWithInvalidInput() {
    assertThat(MiscUtils.parseDate(null)).isEmpty();
    assertThat(MiscUtils.parseDate("")).isEmpty();
    assertThat(MiscUtils.parseDate("INVALID")).isEmpty();
    assertThat(MiscUtils.parseDate("04/04/24")).isEmpty();
  }

  @Test
  void testFormatDate() {
    assertThat(formatDate(LocalDate.of(2024, APRIL, 1))).isEqualTo("040124");
  }

  @ParameterizedTest(name = "[{index}] => \"{0}\" yields \"{1}\"")
  @CsvSource({"2021,2021 - 2025", "2024,2021 - 2025", "2025, 2021 - 2025", "2026,2026 - 2030",
      "2030,2026 - 2030"})
  void testGet5YearRange(int year, String expectedRange) {
    assertThat(MiscUtils.get5YearRange(year)).isEqualTo(expectedRange);
  }
}
