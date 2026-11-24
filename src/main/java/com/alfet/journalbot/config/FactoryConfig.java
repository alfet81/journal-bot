package com.alfet.journalbot.config;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import com.alfet.journalbot.atracker.csv.TaskCsvRecordImpl;
import com.alfet.journalbot.atracker.domain.TaskDetails;
import com.alfet.journalbot.atracker.mapper.TaskDetailsMapper;
import com.alfet.journalbot.atracker.message.gmail.ATrackerGmailMessage;
import com.alfet.journalbot.log.message.gmail.LogGmailMessage;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class FactoryConfig {

  private final TaskDetailsMapper taskDetailsMapper;

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public LogGmailMessage logGmailMessage(Message message) {
    return new LogGmailMessage(message);
  }

  @Bean
  public Function<Message, LogGmailMessage> logGmailMessageFactory() {
    return this::logGmailMessage;
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public ATrackerGmailMessage atrackerGmailMessage(Message message) {
    return new ATrackerGmailMessage(message);
  }

  @Bean
  public Function<Message, ATrackerGmailMessage> atrackerGmailMessageFactory() {
    return this::atrackerGmailMessage;
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public TaskDetails taskDetails(CSVRecord csvRecord) {
    return taskDetailsMapper.toTaskDetails(new TaskCsvRecordImpl(csvRecord));
  }

  @Bean
  public Function<CSVRecord, TaskDetails> taskDetailsFactory() {
    return this::taskDetails;
  }
}
