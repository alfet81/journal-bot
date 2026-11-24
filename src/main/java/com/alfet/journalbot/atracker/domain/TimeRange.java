package com.alfet.journalbot.atracker.domain;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Class for immutable object containing starting date time and ending date time.
 */
@Value
@RequiredArgsConstructor(staticName = "of")
public class TimeRange {

  @NonNull
  LocalDateTime startDateTime;

  @NonNull
  LocalDateTime endDateTime;

  /**
   * Returns duration between starting date time and ending date time as String.
   *
   * @return Duration in hours and minutes as string. (example: 1 hr 10 min)
   */
  public String getDurationAsString() {
    var duration = Duration.between(startDateTime, endDateTime);
    long totalHours = duration.toHours();
    long leftoverMinutes = duration.toMinutes() % 60;
    var result = (totalHours > 0 ? totalHours + " hr " : EMPTY)
        + (leftoverMinutes > 0 ? leftoverMinutes + " min" : EMPTY);
    return result.trim();
  }

  /**
   * Returns boolean value showing whether the supplied date within time range.
   *
   * @param date LocalDate to check
   * @return <code>true</code> if date falls within the time range and <code>false</code> otherwise.
   */
  public boolean encompasses(LocalDate date) {
    var startDate = startDateTime.toLocalDate();
    var endDate = endDateTime.toLocalDate();
    return (startDate.isBefore(date) || startDate.isEqual(date))
        && (endDate.isAfter(date) || endDate.isEqual(date));
  }
}
