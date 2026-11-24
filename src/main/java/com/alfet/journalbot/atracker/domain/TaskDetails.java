package com.alfet.journalbot.atracker.domain;

import module java.base;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.alfet.journalbot.exception.ApplicationException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Class for immutable TaskDetails object. Contains information about time tracker task details.
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskDetails {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

  @NonNull
  String name;

  @NonNull
  TimeRange timeRange;

  String note;

  /**
   * Generates formatted text from the task details.
   *
   * @param currentDate Outside parameter with current date against which this task is matched to
   *                    format the start and end time
   * @return String representation of the task detail.
   */
  public String formatted(LocalDate currentDate) {
    validate(currentDate);

    String startDateTime = formatDateTime(timeRange.getStartDateTime(), currentDate);
    String endDateTime = formatDateTime(timeRange.getEndDateTime(), currentDate);
    String extraNotes = isNotEmpty(note) ? " (" + note + ")" : EMPTY;

    return startDateTime + " - " + endDateTime
        + SPACE + "(" + timeRange.getDurationAsString() + ")"
        + SPACE + name + extraNotes;
  }

  private String formatDateTime(LocalDateTime dateTime, LocalDate currentDate) {
    return dateTime.toLocalDate().equals(currentDate) ?
        dateTime.toLocalTime().format(TIME_FORMATTER) : dateTime.format(DATE_TIME_FORMATTER);
  }

  private void validate(LocalDate currentDate) {
    if (!timeRange.encompasses(currentDate)) {
      throw new ApplicationException("Current date " + currentDate
          + " is outside of time range: " + timeRange);
    }
  }
}
