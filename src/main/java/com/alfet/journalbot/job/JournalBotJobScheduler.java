package com.alfet.journalbot.job;

import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.service.JournalBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The scheduler which enables a job that runs journal bot email reader per cron schedule.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JournalBotJobScheduler {

  private final JournalBotService journalBotService;

  @Scheduled(cron = "${journal-bot.schedule}")
  public void executeJob() {
    try {
      logger.atDebug().log("Running journal bot job.");
      journalBotService.runProcess();
    } catch (ApplicationException e) {
      logger.atError().setCause(e).log("Journal bot job failed.");
    }
  }
}
