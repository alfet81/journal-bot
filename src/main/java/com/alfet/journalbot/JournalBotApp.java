package com.alfet.journalbot;

import static org.springframework.boot.SpringApplication.run;

import com.alfet.journalbot.config.GoogleApiProperties;
import com.alfet.journalbot.config.JournalBotProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({GoogleApiProperties.class, JournalBotProperties.class})
public class JournalBotApp {

  static void main(String... args) {
    run(JournalBotApp.class, args);
  }
}
