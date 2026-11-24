package com.alfet.journalbot.atracker.csv;

import java.time.LocalDateTime;

public interface TaskCsvRecord {

  String getName();

  LocalDateTime getStartDateTime();

  LocalDateTime getEndDateTime();

  String getNote();
}
