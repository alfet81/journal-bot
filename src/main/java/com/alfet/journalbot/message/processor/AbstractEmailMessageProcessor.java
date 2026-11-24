package com.alfet.journalbot.message.processor;

import module java.base;
import static com.alfet.journalbot.util.FileUtils.FOLDER_SEPARATOR;
import static com.alfet.journalbot.util.FileUtils.saveFile;

import com.alfet.journalbot.config.JournalBotProperties;
import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.log.domain.LogRecord;
import com.alfet.journalbot.message.domain.EmailMessage;
import com.alfet.journalbot.message.domain.EmailMessageAttachment;
import com.alfet.journalbot.message.type.MessageType;
import com.alfet.journalbot.service.JournalManagementService;
import com.alfet.journalbot.util.MiscUtils;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractEmailMessageProcessor implements EmailMessageProcessor {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final JournalBotProperties journalBotProperties;
  private final JournalManagementService journalManagementService;

  @Override
  public void process(@NonNull EmailMessage emailMessage) {
    validate(emailMessage.getType());

    var processingDate = getProcessingDate(emailMessage);

    var filePath = getAttachmentsRootDir() + FOLDER_SEPARATOR
        + DATE_FORMATTER.format(processingDate);

    var attachments = emailMessage.getAttachments().stream()
        .map((var attachment) -> attachment.withFilePath(filePath))
        .toList();

    emailMessage = emailMessage.withAttachments(attachments);

    var content = prepareContent(emailMessage, processingDate);

    saveEmailMessageAttachments(attachments);

    var logRecord = LogRecord.of(processingDate, content);

    journalManagementService.processLogRecord(logRecord);
  }

  private void validate(MessageType messageType) {
    if (!Objects.equals(messageType, getSupportedMessageType())) {
      throw new ApplicationException("Message type '" + messageType + "' is not supported");
    }
  }

  protected final Logger getLogger() {
    return LoggerFactory.getLogger(getClass());
  }

  protected final LocalDate getProcessingDate(EmailMessage emailMessage) {
    return parseDateFromSubject(emailMessage.getMetadata().getSubject())
        .orElse(emailMessage.getMetadata().getDateTime().toLocalDate());
  }

  protected String getBodyText(EmailMessage emailMessage) {
    return emailMessage.getText();
  }

  protected String prepareContent(EmailMessage emailMessage, LocalDate processingDate) {
    var footer = emailMessage.getAttachments().stream()
        .map(EmailMessageAttachment::getFullFileName)
        .collect(Collectors.joining("\n"));

    return getBodyText(emailMessage) + "\n\n" + footer + "\n\n";
  }

  private Optional<LocalDate> parseDateFromSubject(String subject) {
    return extractDateFromSubject(subject).flatMap(MiscUtils::parseDate);
  }

  protected abstract Optional<String> extractDateFromSubject(String subject);

  protected abstract MessageType getSupportedMessageType();

  private String getAttachmentsRootDir() {
    return journalBotProperties.getAttachmentsRootDir();
  }

  private void saveEmailMessageAttachments(List<EmailMessageAttachment> attachments) {
    for (var attachment : attachments) {
      saveEmailMessageAttachment(attachment);
    }
  }

  private void saveEmailMessageAttachment(EmailMessageAttachment attachment) {
    getLogger().atDebug().log("Saving attachment: {}", attachment);
    try {
      saveFile(attachment.getFullFileName(), attachment.getContent());
      getLogger().atInfo().log("Successfully saved attachment: {}", attachment);
    } catch (IOException | IllegalArgumentException e) {
      var message = "Failed to save email message attachment with file name '%s' into folder '%s'"
          .formatted(attachment.getFileName(), attachment.getFilePath());
      throw new ApplicationException(message, e);
    }
  }
}
