package com.alfet.journalbot.atracker.csv;

import module java.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.alfet.journalbot.csv.CsvUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TaskCsvRecordImplTest {

  @Value("classpath:csv/CSVFile.csv")
  private Resource csvFileResource;

  @Test
  void constructor_WhenInputValueIsNull() {
    // When
    var exception = catchException(() -> new TaskCsvRecordImpl(null));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("csvRecord is marked non-null but is null");
  }

  @Test
  void testGetters() throws IOException {
    // Given
    String csvData = loadCsvFile();
    var csvReader = new StringReader(csvData);
    var tasks = CsvUtils.readCsvFile(csvReader);
    assertThat(tasks).isNotEmpty().hasSize(28);

    // When
    var taskCsvRecord = new TaskCsvRecordImpl(tasks.get(1));

    // Then
    assertThat(taskCsvRecord.getName()).isEqualTo("Driving");
    assertThat(taskCsvRecord.getStartDateTime())
        .isEqualTo(LocalDateTime.parse("2025-10-04T22:35"));
    assertThat(taskCsvRecord.getEndDateTime())
        .isEqualTo(LocalDateTime.parse("2025-10-04T22:50"));
    assertThat(taskCsvRecord.getNote()).isEqualTo("marathon  home");
  }

  private String loadCsvFile() throws IOException {
    return csvFileResource.getContentAsString(UTF_8);
  }
}
