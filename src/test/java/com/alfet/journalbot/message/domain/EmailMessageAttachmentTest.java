package com.alfet.journalbot.message.domain;

import static com.alfet.journalbot.helper.EmailMessageTestHelper.ATTACHMENT_ID;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.CSV_FILE_NAME;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.FILE_ID;
import static com.alfet.journalbot.util.FileUtils.FOLDER_SEPARATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class EmailMessageAttachmentTest {

  private static final String FILE_PATH = "test/20261026";


  private static Stream<Arguments> provideBuilderInvalidTestArgs() {
    return Stream.of(
        arguments("id is null", null, "ABC", "CSVFile.csv", "id is marked non-null but is null"),
        arguments("fileId is null", "ZYZ", null, "CSVFile.csv",
            "fileId is marked non-null but is null"),
        arguments("fileName is null", "ZYZ", "ABC", null,
            "fileName is marked non-null but is null"));
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideBuilderInvalidTestArgs")
  @DisplayName("builder_withInvalidArguments")
  void builder_withInvalidArguments(String scenario, String id, String fileId, String fileName,
      String expectedMessage) {
    // When
    var exception = catchException(() -> EmailMessageAttachment.builder()
        .id(id)
        .fileId(fileId)
        .fileName(fileName)
        .build());

    // Then
    assertThat(exception).isInstanceOf(IllegalArgumentException.class).hasMessage(expectedMessage);
  }

  @Test
  void getFullFileName_WhenAllDataIsSet() {
    // Given
    var attachment = EmailMessageAttachment.builder()
        .id(ATTACHMENT_ID)
        .fileId(FILE_ID)
        .fileName(CSV_FILE_NAME)
        .filePath(FILE_PATH)
        .build();

    // When & Then
    assertThat(attachment.getFullFileName()).isEqualTo(FILE_PATH + FOLDER_SEPARATOR + CSV_FILE_NAME);
  }


  @Test
  void getFullFileName_WhenFilePathIsMissing() {
    // Given
    var attachment = EmailMessageAttachment.builder()
        .id(ATTACHMENT_ID)
        .fileId(FILE_ID)
        .fileName(CSV_FILE_NAME)
        .build();

    // When & Then
    assertThat(attachment.getFullFileName()).isEqualTo(CSV_FILE_NAME);
  }
}
