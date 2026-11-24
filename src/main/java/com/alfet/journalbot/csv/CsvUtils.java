package com.alfet.journalbot.csv;

import module java.base;

import static org.apache.commons.csv.CSVFormat.RFC4180;

import com.alfet.journalbot.atracker.domain.TaskDetailColumn;
import com.alfet.journalbot.exception.ApplicationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CsvUtils {

  private static final CSVFormat csvFormat = initCSVFormat();

  private static CSVFormat initCSVFormat() {
    return RFC4180.builder()
        .setHeader(TaskDetailColumn.class)
        .setSkipHeaderRecord(true)
        .setIgnoreHeaderCase(true)
        .get();
  }

  public static List<CSVRecord> readCsvFile(@NonNull Reader csvDataReader) {
    try (var parser = csvFormat.parse(csvDataReader)) {
      return parser.getRecords();
    } catch (IOException e) {
      throw new ApplicationException("Failed to parse CSV file. See logs for more details.", e);
    }
  }
}
