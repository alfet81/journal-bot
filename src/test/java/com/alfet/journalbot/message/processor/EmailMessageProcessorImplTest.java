package com.alfet.journalbot.message.processor;

import module java.base;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.alfet.journalbot.atracker.message.processor.ATrackerEmailMessageProcessor;
import com.alfet.journalbot.log.message.processor.LogEmailMessageProcessor;
import com.alfet.journalbot.message.domain.EmailMessage;
import com.alfet.journalbot.message.domain.EmailMessageAttachment;
import com.alfet.journalbot.message.domain.EmailMessageMetadata;
import com.alfet.journalbot.message.type.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailMessageProcessorImplTest {

  private static final String EMAIL_SENDER = "john.doe@gmail.com";
  private static final String EMAIL_RECIPIENT = "journal.bot@gmail.com";
  private static final String EMAIL_SUBJECT = "log 102626";
  private static final String FILE_NAME = "CSVFile.csv";
  private static final LocalDateTime EMAIL_DATE_TIME = LocalDateTime.of(2025, 10, 4, 21, 43, 57);
  private static final String ATTACHMENT_ID = "XYZ";
  private static final String EMAIL_TEXT = "Some text";
  private static final String MESSAGE_ID = "1a1a02b0b9ad1b53";
  private static final String FILE_ID = "ABC";

  @Mock
  private LogEmailMessageProcessor logEmailMessageProcessor;

  @Mock
  private ATrackerEmailMessageProcessor atrackerEmailMessageProcessor;

  @InjectMocks
  private EmailMessageProcessorImpl emailMessageProcessor;

  @Test
  void process_whenMessageTypeIsLog() {
    // Given
    var emailMessage = buildEmailMessage(MessageType.LOG);

    doNothing().when(logEmailMessageProcessor).process(emailMessage);

    // When
    emailMessageProcessor.process(emailMessage);

    // Then
    verify(logEmailMessageProcessor).process(emailMessage);
    verify(atrackerEmailMessageProcessor, never()).process(any(EmailMessage.class));
  }

  @Test
  void process_whenMessageTypeIsATracker() {
    // Given
    var emailMessage = buildEmailMessage(MessageType.A_TRACKER);

    doNothing().when(atrackerEmailMessageProcessor).process(emailMessage);

    // When
    emailMessageProcessor.process(emailMessage);

    // Then
    verify(logEmailMessageProcessor, never()).process(any(EmailMessage.class));
    verify(atrackerEmailMessageProcessor).process(emailMessage);
  }

  @Test
  void process_whenIllegalArgumentExceptionIsThrown() {
    // When
    var exception = catchException(() -> emailMessageProcessor.process(null));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("emailMessage is marked non-null but is null");

    verify(logEmailMessageProcessor, never()).process(any(EmailMessage.class));
    verify(atrackerEmailMessageProcessor, never()).process(any(EmailMessage.class));
  }

  private static EmailMessage buildEmailMessage(MessageType messageType) {
    var metadata = EmailMessageMetadata.builder()
        .recipient(EMAIL_RECIPIENT)
        .sender(EMAIL_SENDER)
        .subject(EMAIL_SUBJECT)
        .dateTime(EMAIL_DATE_TIME)
        .build();
    var attachment = EmailMessageAttachment.builder()
        .id(ATTACHMENT_ID)
        .fileId(FILE_ID)
        .fileName(FILE_NAME)
        .build();
    return EmailMessage.builder()
        .id(MESSAGE_ID)
        .type(messageType)
        .text(EMAIL_TEXT)
        .metadata(metadata)
        .attachments(List.of(attachment))
        .build();
  }
}
