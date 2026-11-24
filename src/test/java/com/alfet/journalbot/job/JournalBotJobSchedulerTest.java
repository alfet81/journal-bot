package com.alfet.journalbot.job;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.service.JournalBotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JournalBotJobSchedulerTest {

  @Mock
  private JournalBotService journalBotService;

  @InjectMocks
  private JournalBotJobScheduler jobScheduler;


  @Test
  void executeJob() {
    // Given
    doNothing().when(journalBotService).runProcess();

    // When
    jobScheduler.executeJob();

    // Then
    verify(journalBotService).runProcess();
  }

  @Test
  void executeJob_WhenApplicationExceptionIsThrown() {
    // Given
    doThrow(new ApplicationException("Test")).when(journalBotService).runProcess();

    // When
    assertThatCode(() -> jobScheduler.executeJob()).doesNotThrowAnyException();

    // Then
    verify(journalBotService).runProcess();
  }

  @Test
  void executeJob_WhenOtherRuntimeExceptionIsThrown() {
    // Given
    doThrow(new IllegalStateException("Nesty exception")).when(journalBotService).runProcess();

    // When
    assertThatThrownBy(() -> jobScheduler.executeJob()).isInstanceOf(IllegalStateException.class);

    // Then
    verify(journalBotService).runProcess();
  }
}
