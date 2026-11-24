package com.alfet.journalbot.atracker.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

class TimeRangeTest {

  private static final LocalDateTime START_DATE_TIME_1 = LocalDateTime.parse("2026-10-24T10:00:00");
  private static final LocalDateTime END_DATE_TIME_1 = LocalDateTime.parse("2026-10-24T12:00:00");
  private static final LocalDateTime START_DATE_TIME_2 = LocalDateTime.parse("2026-10-23T10:00:00");
  private static final LocalDateTime END_DATE_TIME_2 = LocalDateTime.parse("2026-10-25T12:00:00");


  private static Stream<Arguments> provideBuilderTestArgs() {
    return Stream.of(
        arguments("startDateTime is null", null, LocalDateTime.now(),
            "startDateTime is marked non-null but is null"),
        arguments("endDateTime is null", LocalDateTime.now(), null,
            "endDateTime is marked non-null but is null")
    );
  }

  private static Stream<Arguments> provideDurationTestArgs() {
    return Stream.of(
        arguments("Duration in hours only",
            TimeRange.of(LocalDateTime.parse("2026-10-24T10:00:00"),
                LocalDateTime.parse("2026-10-24T11:00:00")), "1 hr"),
        arguments("Duration in minutes only",
            TimeRange.of(LocalDateTime.parse("2026-10-24T10:00:00"),
                LocalDateTime.parse("2026-10-24T10:20:00")), "20 min"),
        arguments("Duration in both: hours and minutes",
            TimeRange.of(LocalDateTime.parse("2026-10-24T10:00:00"),
                LocalDateTime.parse("2026-10-24T11:30:00")), "1 hr 30 min")
    );
  }

  private static Stream<Arguments> provideEncompassesTestArgs() {
    var date = LocalDate.of(2026, 10, 24);
    return Stream.of(
        arguments("supplied date before startDate and endDate", date.minusDays(1),
            TimeRange.of(START_DATE_TIME_1, END_DATE_TIME_1), false),
        arguments("supplied date equals startDate and endDate", date,
            TimeRange.of(START_DATE_TIME_1, END_DATE_TIME_1), true),
        arguments("supplied date after startDate and equals endDate", date,
            TimeRange.of(START_DATE_TIME_2, END_DATE_TIME_1), true),
        arguments("supplied date equals startDate and before endDate", date,
            TimeRange.of(START_DATE_TIME_1, END_DATE_TIME_2), true),
        arguments("supplied after startDate and endDate", date.plusDays(1),
            TimeRange.of(START_DATE_TIME_1, END_DATE_TIME_1), false)
    );
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideBuilderTestArgs")
  @DisplayName("builder_WithInvalidArguments")
  void builder_WithInvalidArguments(String scenario, LocalDateTime startDateTime,
      LocalDateTime endDateTime, String expectedErrorMessage) {
    // When
    var exception = catchException(() -> TimeRange.of(startDateTime, endDateTime));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(expectedErrorMessage);
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideDurationTestArgs")
  @DisplayName("getDurationAsString")
  void getDurationAsString(String scenario, TimeRange timeRange, String expectedDurationAsString) {
    // When
    var duration = timeRange.getDurationAsString();

    // Then
    assertThat(duration).isEqualTo(expectedDurationAsString);
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideEncompassesTestArgs")
  @DisplayName("encompasses")
  void encompasses(String scenario, LocalDate date, TimeRange timeRange, boolean expectedResult) {
    assertThat(timeRange.encompasses(date)).isEqualTo(expectedResult);
  }
}
