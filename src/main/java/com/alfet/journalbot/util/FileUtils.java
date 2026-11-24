package com.alfet.journalbot.util;

import module java.base;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.time.format.DateTimeFormatter.ISO_DATE;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

  public static final String FOLDER_SEPARATOR = FileSystems.getDefault().getSeparator();

  public static void saveFile(@NonNull String filePath, @NonNull byte[] content)
      throws IOException {
    if (content.length == 0) {
      logger.atWarn().log("File content is empty. Nothing to save.");
      return;
    }
    var subPath = filePath.substring(0, filePath.lastIndexOf(FOLDER_SEPARATOR));
    var dirPath = Paths.get(subPath);

    Files.createDirectories(dirPath);

    int index = 0;
    var resultingPath = filePath;

    while (Files.exists(Path.of(resultingPath))) {
      resultingPath = addFileCounter(filePath, ++index);
    }
    Files.write(Path.of(resultingPath), content, CREATE_NEW);
  }

  private static String addFileCounter(String fileName, int counter) {
    var dotPos = fileName.lastIndexOf(".");
    var prefix = fileName.substring(0, dotPos);
    var postfix = fileName.substring(dotPos);
    return prefix + "_" + counter + postfix;
  }

  public static File createTempFile(@NonNull String fileName, @NonNull byte[] fileContent,
      File directory) throws IOException {
    var suffix = LocalDate.now().format(ISO_DATE);
    var tempFile = File.createTempFile(fileName, suffix, directory);
    try (var fileOutputStream = new FileOutputStream(tempFile)) {
      fileOutputStream.write(fileContent);
      return tempFile;
    }
  }
}
