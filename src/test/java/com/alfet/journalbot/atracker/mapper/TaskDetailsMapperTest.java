package com.alfet.journalbot.atracker.mapper;

import module java.base;

import static org.assertj.core.api.Assertions.assertThat;

import com.alfet.journalbot.atracker.csv.TaskCsvRecordImpl;
import com.alfet.journalbot.atracker.domain.TaskDetailColumn;
import com.alfet.journalbot.atracker.domain.TimeRange;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

class TaskDetailsMapperTest {

  private static final String CSV_DATA = """
      Task name, Task description, Start time, End time, Duration,Duration in hours, Note, Tag
      "Driving","Lexus/Uhaul/Loaner","Oct 4, 2025 at 10:35:00PM","Oct 4, 2025 at 10:50:00PM","00:15:00",0.2500027,"marathon  home","Commute "
      """;

  private final TaskDetailsMapper taskDetailsMapper = new TaskDetailsMapperImpl();

  @Test
  void toTaskDetails_WhenValidDataPresent() throws IOException {
    // Given
    var taskCsvRecord = new TaskCsvRecordImpl(getCSVRecord());

    // When
    var result = taskDetailsMapper.toTaskDetails(taskCsvRecord);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(taskCsvRecord.getName());
    assertThat(result.getTimeRange())
        .isEqualTo(TimeRange.of(taskCsvRecord.getStartDateTime(), taskCsvRecord.getEndDateTime()));
    assertThat(result.getNote()).isEqualTo(taskCsvRecord.getNote());
  }

  private static CSVRecord getCSVRecord() throws IOException {
    try (var parser = CSVFormat.RFC4180.builder()
        .setHeader(TaskDetailColumn.class).setSkipHeaderRecord(true).get()
        .parse(new StringReader(CSV_DATA))) {
      return parser.getRecords().getFirst();
    }
  }

  @Test
  void toTaskDetails_WhenNullPassed() {
    // When
    var result = taskDetailsMapper.toTaskDetails(null);

    // Then
    assertThat(result).isNull();
  }
}
