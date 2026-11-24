package com.alfet.journalbot.html;

import module java.base;

import static com.alfet.journalbot.util.MiscUtils.asOptional;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RussianDayOfWeek {
  MON(MONDAY, "Пн"),
  TUE(TUESDAY, "Вт"),
  WED(WEDNESDAY, "Ср"),
  THU(THURSDAY, "Чт"),
  FRI(FRIDAY, "Пт"),
  SAT(SATURDAY, "Сб"),
  SUN(SUNDAY, "Вс");

  private static final Map<DayOfWeek, RussianDayOfWeek> MAP = init();

  private final DayOfWeek dayOfWeek;
  private final String value;

  public static Optional<String> getValue(DayOfWeek dayOfWeek) {
    return getByDayOfWeek(dayOfWeek).map(RussianDayOfWeek::getValue);
  }

  private static Optional<RussianDayOfWeek> getByDayOfWeek(DayOfWeek dayOfWeek) {
    return asOptional(MAP.get(dayOfWeek));
  }

  private static Map<DayOfWeek, RussianDayOfWeek> init() {
    return Stream.of(values()).collect(toMap(
        RussianDayOfWeek::getDayOfWeek,
        identity(),
        (k1, _) -> k1,
        () -> new EnumMap<>(DayOfWeek.class)));
  }
}
