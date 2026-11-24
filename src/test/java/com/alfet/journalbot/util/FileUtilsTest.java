package com.alfet.journalbot.util;

import module java.base;
import static com.alfet.journalbot.util.FileUtils.createTempFile;
import static com.alfet.journalbot.util.FileUtils.saveFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FileUtilsTest {

  @Test
  void testSaveFile(@TempDir Path tempDir) throws IOException {
    // Given
    List<String> fileNames = List.of("/test/img_1234.jpg", "/test/img_1234.jpg",
        "/test/img_0000.jpg");

    // When
    for (String fileName : fileNames) {
      var path = tempDir.toAbsolutePath() + fileName;
      saveFile(path, "TEST".getBytes());
    }

    // Then
    assertThat(Files.exists(Path.of(tempDir.toString(), fileNames.get(0)))).isTrue();
    assertThat(Files.exists(Path.of(tempDir.toString(), "/test/img_1234_1.jpg"))).isTrue();
    assertThat(Files.exists(Path.of(tempDir.toString(), fileNames.get(2)))).isTrue();
  }

  @Test
  void testSaveFile_WhenFilePathIsNull() {
    // When
    var exception = catchException(() -> saveFile(null, "TEST".getBytes()));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("filePath is marked non-null but is null");
  }

  @Test
  void testSaveFile_WhenContentIsNull(@TempDir Path tempDir) {
    // Given
    var path = tempDir.toAbsolutePath() + "/test/img_1234.jpg";

    // When
    var exception = catchException(() -> saveFile(path, null));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("content is marked non-null but is null");
  }

  @Test
  void testSaveFile_WhenContentSizeIsZero(@TempDir Path tempDir) throws IOException {
    // Given
    var path = tempDir.toAbsolutePath() + "/test/img_1234.jpg";

    // When
    saveFile(path, new byte[0]);

    // Then
    assertThat(Files.exists(Path.of(tempDir.toString(), "/test/img_1234.jpg"))).isFalse();
  }

  private static Stream<Arguments> provideCreateTempFileInvalidTestArgs() {
    return Stream.of(
        arguments("fileName is null", null, new byte[0], "fileName is marked non-null but is null"),
        arguments("fileContent is null", "Test", null, "fileContent is marked non-null but is null")
    );
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideCreateTempFileInvalidTestArgs")
  @DisplayName("testCreateTempFile_WhenInvalidInput")
  void testCreateTempFile_WhenInvalidInput(String scenario, String fileName, byte[] fileContent,
      String expectedErrorMessage) {
    // When
    var exception = catchException(() -> createTempFile(fileName, fileContent, null));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(expectedErrorMessage);
  }
}
