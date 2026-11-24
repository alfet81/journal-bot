package com.alfet.journalbot.atracker.csv;

import module java.base;

import static com.alfet.journalbot.atracker.domain.TaskDetailColumn.END_TIME;
import static com.alfet.journalbot.atracker.domain.TaskDetailColumn.NOTE;
import static com.alfet.journalbot.atracker.domain.TaskDetailColumn.START_TIME;
import static com.alfet.journalbot.atracker.domain.TaskDetailColumn.TASK_NAME;

import com.alfet.journalbot.atracker.domain.TaskDetailColumn;
import com.alfet.journalbot.util.DateTimeUtils;
import lombok.NonNull;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

public class TaskCsvRecordImpl implements TaskCsvRecord {

  private final Map<String, String> csvRecordAsMap;

  public TaskCsvRecordImpl(@NonNull CSVRecord csvRecord) {
    csvRecordAsMap = csvRecord.toMap();
  }

  @Override
  public String getName() {
    return StringUtils.trim(getValue(TASK_NAME));
  }

  @Override
  public LocalDateTime getStartDateTime() {
    var startTime = getValue(START_TIME);
    return DateTimeUtils.parse(startTime).orElse(LocalDateTime.now());
  }

  @Override
  public LocalDateTime getEndDateTime() {
    var endTime = getValue(END_TIME);
    return DateTimeUtils.parse(endTime).orElse(LocalDateTime.now());
  }

  @Override
  public String getNote() {
    return StringUtils.trim(getValue(NOTE));
  }

  private String getValue(TaskDetailColumn column) {
    return csvRecordAsMap.get(column.name());
  }
}
