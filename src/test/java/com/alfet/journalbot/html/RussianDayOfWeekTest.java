package com.alfet.journalbot.html;

import module java.base;
import static com.alfet.journalbot.html.RussianDayOfWeek.FRI;
import static com.alfet.journalbot.html.RussianDayOfWeek.MON;
import static com.alfet.journalbot.html.RussianDayOfWeek.SAT;
import static com.alfet.journalbot.html.RussianDayOfWeek.SUN;
import static com.alfet.journalbot.html.RussianDayOfWeek.THU;
import static com.alfet.journalbot.html.RussianDayOfWeek.TUE;
import static com.alfet.journalbot.html.RussianDayOfWeek.WED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.TUESDAY;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

@Isolated
class RussianDayOfWeekTest {

  private static Stream<Arguments> provideTestArgs() {
    return Stream.of(
        arguments("Monday", MON.getDayOfWeek(), "Пн"),
        arguments("Tuesday", TUE.getDayOfWeek(), "Вт"),
        arguments("Wednesday", WED.getDayOfWeek(), "Ср"),
        arguments("Thursday", THU.getDayOfWeek(), "Чт"),
        arguments("Friday", FRI.getDayOfWeek(), "Пт"),
        arguments("Saturday", SAT.getDayOfWeek(), "Сб"),
        arguments("Sunday", SUN.getDayOfWeek(), "Вс")
    );
  }

  @Order(1)
  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideTestArgs")
  @DisplayName("testGetValue")
  void testGetValue(String scenario, DayOfWeek dayOfWeek, String expectedValue) {
    assertThat(RussianDayOfWeek.getValue(dayOfWeek)).hasValue(expectedValue);
  }

  @Order(2)
  @Test
  void testInit_WhenNonUniqueDayOfWeek() {
    // Given
    ReflectionTestUtils.setField(RussianDayOfWeek.TUE, "dayOfWeek", MONDAY);

    // When
    Map<DayOfWeek, RussianDayOfWeek> actualResult =
        ReflectionTestUtils.invokeMethod(RussianDayOfWeek.class, "init");

    // Then
    assertThat(actualResult).isNotNull();
    assertThat(actualResult.get(MONDAY)).isEqualTo(MON);
    assertThat(actualResult.get(TUESDAY)).isNull();

    // Rollback changes
    ReflectionTestUtils.setField(RussianDayOfWeek.TUE, "dayOfWeek", TUESDAY);
  }
}
