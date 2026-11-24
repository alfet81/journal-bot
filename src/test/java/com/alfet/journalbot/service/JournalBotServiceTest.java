package com.alfet.journalbot.service;

import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_RECIPIENT;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SENDER_STRIPPED;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.EMAIL_SUBJECT;
import static com.alfet.journalbot.helper.EmailMessageTestHelper.MESSAGE_ID;
import static com.alfet.journalbot.message.type.MessageType.A_TRACKER;
import static com.alfet.journalbot.message.type.MessageType.LOG;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alfet.journalbot.atracker.message.provider.ATrackerEmailMessageProvider;
import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.log.message.provider.LogEmailMessageProvider;
import com.alfet.journalbot.message.domain.EmailMessage;
import com.alfet.journalbot.message.domain.EmailMessageMetadata;
import com.alfet.journalbot.message.processor.EmailMessageProcessor;
import com.alfet.journalbot.message.provider.EmailMessageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class JournalBotServiceTest {

  @Mock
  private ATrackerEmailMessageProvider aTrackerEmailMessageProvider;

  @Mock
  private LogEmailMessageProvider logEmailMessageProvider;

  @Mock
  private EmailMessageProcessor emailMessageProcessor;

  private JournalBotService journalBotService;

  @BeforeEach
  void setup() {
    List<EmailMessageProvider> emailMessageProviders =
        List.of(aTrackerEmailMessageProvider, logEmailMessageProvider);
    journalBotService = new JournalBotService(emailMessageProviders, emailMessageProcessor);
  }

  @Test
  void runProcess_WhenNoMessagesReturned() {
    // Given
    when(aTrackerEmailMessageProvider.getEmailMessages()).thenReturn(List.of());
    when(logEmailMessageProvider.getEmailMessages()).thenReturn(List.of());

    // When
    journalBotService.runProcess();

    // Then
    verify(aTrackerEmailMessageProvider).getEmailMessages();
    verify(logEmailMessageProvider).getEmailMessages();
    verify(emailMessageProcessor, never()).process(any(EmailMessage.class));
  }


  @Test
  void runProcess_WhenAtrackerMessagesReturnedOnly() {
    // Given
    when(aTrackerEmailMessageProvider.getEmailMessages()).thenReturn(buildAtrackerEmailMessages());
    when(logEmailMessageProvider.getEmailMessages()).thenReturn(List.of());
    doNothing().when(emailMessageProcessor).process(any(EmailMessage.class));

    // When
    journalBotService.runProcess();

    // Then
    verify(aTrackerEmailMessageProvider).getEmailMessages();
    verify(logEmailMessageProvider).getEmailMessages();
    verify(emailMessageProcessor).process(any(EmailMessage.class));
  }

  @Test
  void runProcess_WhenLogMessagesReturnedOnly() {
    // Given
    when(aTrackerEmailMessageProvider.getEmailMessages()).thenReturn(List.of());
    when(logEmailMessageProvider.getEmailMessages()).thenReturn(buildLogEmailMessages());
    doNothing().when(emailMessageProcessor).process(any(EmailMessage.class));

    // When
    journalBotService.runProcess();

    // Then
    verify(aTrackerEmailMessageProvider).getEmailMessages();
    verify(logEmailMessageProvider).getEmailMessages();
    verify(emailMessageProcessor).process(any(EmailMessage.class));
  }

  @Test
  void runProcess_WhenBothTypeMessagesReturned() {
    // Given
    when(aTrackerEmailMessageProvider.getEmailMessages()).thenReturn(buildAtrackerEmailMessages());
    when(logEmailMessageProvider.getEmailMessages()).thenReturn(buildLogEmailMessages());
    doNothing().when(emailMessageProcessor).process(any(EmailMessage.class));

    // When
    journalBotService.runProcess();

    // Then
    verify(aTrackerEmailMessageProvider).getEmailMessages();
    verify(logEmailMessageProvider).getEmailMessages();
    verify(emailMessageProcessor, times(2)).process(any(EmailMessage.class));
  }

  @Test
  void runProcess_WhenApplicationExceptionThrown() {
    // Given
    when(aTrackerEmailMessageProvider.getEmailMessages()).thenReturn(buildAtrackerEmailMessages());
    when(logEmailMessageProvider.getEmailMessages()).thenReturn(buildLogEmailMessages());
    doThrow(new ApplicationException("Something went wrong"))
        .when(emailMessageProcessor).process(any(EmailMessage.class));

    // When
    assertThatCode(() -> journalBotService.runProcess()).doesNotThrowAnyException();

    // Then
    verify(aTrackerEmailMessageProvider).getEmailMessages();
    verify(logEmailMessageProvider).getEmailMessages();
    verify(emailMessageProcessor, times(2)).process(any(EmailMessage.class));
  }

  private static List<EmailMessage> buildAtrackerEmailMessages() {
    var metadata = EmailMessageMetadata.builder()
        .sender(EMAIL_SENDER_STRIPPED)
        .recipient(EMAIL_RECIPIENT)
        .dateTime(LocalDateTime.now())
        .subject(EMAIL_SUBJECT)
        .build();
    var emailMessage = EmailMessage.builder()
        .id(MESSAGE_ID)
        .type(A_TRACKER)
        .metadata(metadata)
        .text(EMPTY)
        .build();
    return List.of(emailMessage);
  }

  private static List<EmailMessage> buildLogEmailMessages() {
    var metadata = EmailMessageMetadata.builder()
        .sender(EMAIL_SENDER_STRIPPED)
        .recipient(EMAIL_RECIPIENT)
        .dateTime(LocalDateTime.now().plusHours(1))
        .subject(EMAIL_SUBJECT)
        .build();
    var emailMessage = EmailMessage.builder()
        .id(MESSAGE_ID)
        .type(LOG)
        .metadata(metadata)
        .text(EMPTY)
        .build();
    return List.of(emailMessage);
  }
}
