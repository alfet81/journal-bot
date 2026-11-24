package com.alfet.journalbot.message.provider;

import module java.base;
import static lombok.AccessLevel.PROTECTED;

import static java.util.Collections.emptyList;

import com.alfet.journalbot.config.JournalBotProperties;
import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.google.api.GmailService;
import com.alfet.journalbot.message.domain.EmailMessage;
import com.alfet.journalbot.message.domain.EmailMessageAttachment;
import com.alfet.journalbot.message.gmail.GmailMessage;
import com.alfet.journalbot.message.mapper.EmailMessageMapper;
import com.google.api.services.gmail.model.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base abstract class implementing <code>EmailMessageProvider</code> interface. Provides default
 * implementation of <code>EmailMessage</code> extraction from Gmail account. Concrete
 * implementations must implement on abstract method for email message filter query.
 */
@RequiredArgsConstructor
public abstract class AbstractEmailMessageProvider<T extends GmailMessage>
    implements EmailMessageProvider {

  @Getter(PROTECTED)
  private final JournalBotProperties journalBotProperties;

  private final GmailService gmailService;
  private final EmailMessageMapper emailMessageMapper;
  private final Function<Message, T> gmailMessageFactory;

  protected abstract String getMessageFilterQuery();

  protected Logger getLogger() {
    return LoggerFactory.getLogger(getClass());
  }

  protected GmailMessage getGmailMessage(Message message) {
    return gmailMessageFactory.apply(message);
  }

  @Override
  public List<EmailMessage> getEmailMessages() {
    try {
      return getMessagesFromGmail().stream()
          .map(this::processMessage)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();
    } catch (ApplicationException e) {
      getLogger().atError().setCause(e).log("Failed to read emails");
      return emptyList();
    }
  }

  private List<Message> getMessagesFromGmail() {

    var messages = gmailService.getMessages(getMessageFilterQuery());

    getLogger().atInfo().log("Found {} new message(s) for filter query '{}'", messages.size(),
            getMessageFilterQuery());

    return messages;
  }

  protected Optional<EmailMessage> processMessage(Message message) {

    getLogger().atDebug().log("Processing message...");

    try {
      var emailMessage = buildEmailMessage(message);
      emailMessage = processAttachments(emailMessage);

      getLogger().atDebug().log("Created {}", emailMessage);

      return Optional.of(emailMessage);
    } catch (ApplicationException | IllegalArgumentException e) {
      getLogger().atError().setCause(e).log("Failed to process email message");
      return Optional.empty();
    }
  }

  private EmailMessage processAttachments(EmailMessage emailMessage) {
    getLogger().atDebug().log("Processing attachments...");
    var attachments = downloadAttachments(emailMessage);
    getLogger().atInfo().log("Downloaded {} attachment(s)", attachments.size());
    return emailMessage.withAttachments(attachments);
  }

  private EmailMessage buildEmailMessage(Message message) {
    getLogger().atDebug().log("Building EmailMessage from message details...");
    var gmailMessage = getGmailMessage(message);
    return emailMessageMapper.mapEmailMessage(gmailMessage);
  }

  protected Optional<EmailMessageAttachment> downloadAttachment(EmailMessage emailMessage,
      EmailMessageAttachment attachment) {
    try {
      attachment = downloadEmailMessageAttachment(emailMessage, attachment);
      return Optional.of(attachment);
    } catch (ApplicationException e) {
      getLogger().atWarn().setCause(e)
          .log("Failed to download email message attachment: " + attachment);
      return Optional.empty();
    }
  }

  protected List<EmailMessageAttachment> downloadAttachments(EmailMessage emailMessage) {
    var downloadedEmailAttachments = new ArrayList<EmailMessageAttachment>();
    for (var attachment : emailMessage.getAttachments()) {
      downloadAttachment(emailMessage, attachment).ifPresent(downloadedEmailAttachments::add);
    }
    return List.copyOf(downloadedEmailAttachments);
  }

  private EmailMessageAttachment downloadEmailMessageAttachment(EmailMessage emailMessage,
      EmailMessageAttachment attachment) {
    getLogger().atDebug().log("Downloading attachment: {}", attachment);
    try {
      var content = gmailService.getMessageAttachmentContent(emailMessage.getId(),
          attachment.getId());
      getLogger().atInfo().log("Successfully downloaded attachment: {}", attachment);
      return attachment.withContent(content);
    } catch (IOException | IllegalArgumentException e) {
      throw new ApplicationException("Failed to download email message attachment"
          + " from Google API for id: " + attachment.getId(), e);
    }
  }
}
