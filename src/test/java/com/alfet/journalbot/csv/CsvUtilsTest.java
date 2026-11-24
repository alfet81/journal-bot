package com.alfet.journalbot.csv;

import module java.base;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.alfet.journalbot.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class CsvUtilsTest {

  @Value("classpath:csv/CSVFile.csv")
  private Resource csvFileResource;

  @Test
  void readCsvFile_WhenWalidData() throws IOException {
    // Given
    String csvData = loadCsvFile();
    var csvReader = new StringReader(csvData);

    // When
    var tasks = CsvUtils.readCsvFile(csvReader);

    // Then
    assertThat(tasks).isNotEmpty().hasSize(28);
  }

  @Test
  void readCsvFile_WhenIOExceptionProduced() throws IOException {
    // Given
    var mockReader = mock(Reader.class);
    when(mockReader.read(any(char[].class), anyInt(), anyInt()))
        .thenThrow(new IOException("Simulated network or disk failure"));

    // When
    var exception = catchException(() -> CsvUtils.readCsvFile(mockReader));

    // Then
    assertThat(exception)
        .isInstanceOf(ApplicationException.class)
        .hasMessage("Failed to parse CSV file. See logs for more details.");
  }

  @Test
  void readCsvFile_WhenReaderIsNull() {
    // When
    var exception = catchException(() -> CsvUtils.readCsvFile(null));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("csvDataReader is marked non-null but is null");
  }

  private String loadCsvFile() throws IOException {
    return csvFileResource.getContentAsString(UTF_8);
  }
}
