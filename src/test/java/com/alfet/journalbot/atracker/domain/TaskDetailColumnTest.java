package com.alfet.journalbot.atracker.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TaskDetailColumnTest {

  @ParameterizedTest(name = "[{index}] => {0}")
  @EnumSource(TaskDetailColumn.class)
  @DisplayName("toString")
  void toString(TaskDetailColumn taskDetailColumn) {
    assertThat(taskDetailColumn.toString()).isEqualTo(taskDetailColumn.getColumnName());
  }
}
