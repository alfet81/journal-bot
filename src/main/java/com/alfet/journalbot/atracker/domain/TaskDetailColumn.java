package com.alfet.journalbot.atracker.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum of supported CSV record columns.
 */
@Getter
@RequiredArgsConstructor
public enum TaskDetailColumn {
  TASK_NAME("Task name"),
  TASK_DESC("Task description"),
  START_TIME("Start time"),
  END_TIME("End time"),
  DURATION("Duration"),
  DURATION_IN_HOURS("Duration in hours"),
  NOTE("Note"),
  TAG("Tag");

  private final String columnName;

  @Override
  public String toString() {
    return columnName;
  }
}
