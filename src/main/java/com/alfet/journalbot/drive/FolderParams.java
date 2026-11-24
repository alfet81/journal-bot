package com.alfet.journalbot.drive;

import module java.base;

import com.google.api.services.drive.model.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FolderParams {

  private LocalDate date;
  private File currentFolder;

  public String getCurrentFolderName() {
    return currentFolder != null ? currentFolder.getName() : null;
  }

  public String getCurrentFolderId() {
    return currentFolder != null ? currentFolder.getId() : null;
  }
}
