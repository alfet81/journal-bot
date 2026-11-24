package com.alfet.journalbot.atracker.domain;

import module java.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.alfet.journalbot.exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class TaskDetailsTest {

  private static Stream<Arguments> provideBuilderInvalidTestArgs() {
    return Stream.of(
        arguments("name is null", null, TimeRange.of(LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(1)), "name is marked non-null but is null"),
        arguments("timeRange is null", "Some name", null,
            "timeRange is marked non-null but is null")
    );
  }

  private static Stream<Arguments> provideFormattedTestArgs() {
    var currentDate = LocalDate.of(2026, 10, 24);
    var timeRange1 = TimeRange.of(LocalDateTime.parse("2026-10-24T10:00:00"),
        LocalDateTime.parse("2026-10-24T12:00:00"));
    var timeRange2 = TimeRange.of(LocalDateTime.parse("2026-10-23T23:00:00"),
        LocalDateTime.parse("2026-10-24T01:00:00"));
    var timeRange3 = TimeRange.of(LocalDateTime.parse("2026-10-24T22:00:00"),
        LocalDateTime.parse("2026-10-25T01:00:00"));

    return Stream.of(
        arguments("startDate and endDate are equal to currentDate", currentDate, timeRange1,
            "10:00 AM - 12:00 PM (2 hr) Routine (Handing around)"),
        arguments("startDate is before currentDate and endDate is equal to currentDate",
            currentDate, timeRange2,
            "23/10/2026 11:00 PM - 1:00 AM (2 hr) Routine (Handing around)"),
        arguments("startDate is equal to currentDate and endDate is after currentDate",
            currentDate, timeRange3,
            "10:00 PM - 25/10/2026 1:00 AM (3 hr) Routine (Handing around)")
    );
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideBuilderInvalidTestArgs")
  @DisplayName("builder_WhenInvalidArguments")
  void builder_WhenInvalidArguments(String scenario, String name, TimeRange timeRange,
      String expectedErrorMsg) {
    // When
    var exception = catchException(() ->
        TaskDetails.builder().name(name).timeRange(timeRange).build());

    // Then
    assertThat(exception).isInstanceOf(IllegalArgumentException.class).hasMessage(expectedErrorMsg);
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideFormattedTestArgs")
  @DisplayName("formatted_WhenCurrentDateIsSameAsStartAndEndDateTime")
  void formatted_WhenValidCurrentDate(String scenario, LocalDate currentDate,
      TimeRange timeRange, String expectedFormattedText) {
    // Given
    var taskDetails = TaskDetails.builder()
        .name("Routine")
        .timeRange(timeRange)
        .note("Handing around")
        .build();

    // When
    var actualResult = taskDetails.formatted(currentDate);

    // Then
    assertThat(actualResult).isEqualTo(expectedFormattedText);
  }

  @ParameterizedTest(name = "[{index}] => Using note value: {0}")
  @NullAndEmptySource
  void formatted_WhenNoteIsMissing(String note) {
    // Given
    var startDateTime = LocalDateTime.parse("2026-10-24T10:00:00");
    var endDateTime = LocalDateTime.parse("2026-10-24T12:00:00");
    var timeRange = TimeRange.of(startDateTime, endDateTime);

    var taskDetails = TaskDetails.builder()
        .name("Routine")
        .timeRange(timeRange)
        .note(note)
        .build();

    // When
    var actualResult = taskDetails.formatted(LocalDate.of(2026, 10, 24));

    // Then
    assertThat(actualResult).isEqualTo("10:00 AM - 12:00 PM (2 hr) Routine");
  }

  @Test
  void formatted_WhenInvalidCurrentDate() {
    // Given
    var currentDate = LocalDate.of(2026, 10, 20);
    var startDateTime = LocalDateTime.parse("2026-10-24T10:00:00");
    var endDateTime = LocalDateTime.parse("2026-10-24T12:00:00");
    var timeRange = TimeRange.of(startDateTime, endDateTime);

    var taskDetails = TaskDetails.builder()
        .name("Routine")
        .timeRange(timeRange)
        .build();

    // When
    var exception = catchException(() -> taskDetails.formatted(currentDate));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasMessage("Current date 2026-10-20 is outside of time range: "
            + "TimeRange(startDateTime=2026-10-24T10:00, endDateTime=2026-10-24T12:00)");
  }
}
