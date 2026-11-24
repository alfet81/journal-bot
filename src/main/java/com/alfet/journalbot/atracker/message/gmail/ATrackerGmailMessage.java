package com.alfet.journalbot.atracker.message.gmail;

import module java.base;
import static com.alfet.journalbot.google.api.types.GmailMimeType.APPLICATION_PDF;
import static com.alfet.journalbot.google.api.types.GmailMimeType.MULTIPART_MIXED;
import static com.alfet.journalbot.google.api.types.GmailMimeType.TEXT_CSV;
import static com.alfet.journalbot.google.api.types.GmailMimeType.TEXT_PLAIN;
import static com.alfet.journalbot.message.type.MessageType.A_TRACKER;
import static com.alfet.journalbot.util.MiscUtils.asOptional;

import static java.util.stream.Collectors.toUnmodifiableList;

import com.alfet.journalbot.message.gmail.AbstractGmailMessage;
import com.alfet.journalbot.message.gmail.GmailMessageAttachment;
import com.alfet.journalbot.message.gmail.GmailMessageAttachmentImpl;
import com.alfet.journalbot.message.type.MessageType;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.MimeType;

/**
 * Class adapter for Gmail messages. Implements <code>GmailMessage</code> interface to convert data
 * from <code>Message</code> to <code>GmailMessage</code>. Implementation is specific to ATracker
 * email types.
 */
public final class ATrackerGmailMessage extends AbstractGmailMessage {

  private static final Set<MimeType> ATTACHMENT_MIME_TYPES = Set.of(TEXT_CSV.getMimeType(),
      APPLICATION_PDF.getMimeType());

  private static final Predicate<MessagePart> IS_MIXED_MULTIPART = (var part) ->
      MimeType.valueOf(part.getMimeType()).equals(MULTIPART_MIXED.getMimeType());

  private static final Predicate<MessagePart> IS_TEXT_PLAIN = (var part) ->
      MimeType.valueOf(part.getMimeType()).equals(TEXT_PLAIN.getMimeType());

  private static final Predicate<MessagePart> IS_SUPPORTED_ATTACHMENT = (var part) ->
      ATTACHMENT_MIME_TYPES.contains(MimeType.valueOf(part.getMimeType()));

  public ATrackerGmailMessage(Message message) {
    super(message);
  }

  @Override
  public MessageType getType() {
    return A_TRACKER;
  }

  @Override
  public String getText() {
    return extractEmailMessageBody().orElse(super.getText());
  }

  @Override
  public List<GmailMessageAttachment> getAttachments() {
    return extractAttachmentMessageParts().stream()
        .map(GmailMessageAttachmentImpl::new)
        .collect(toUnmodifiableList());
  }

  private Optional<String> extractEmailMessageBody() {
    return getMessageParts().stream()
        .filter(IS_TEXT_PLAIN)
        .findFirst()
        .map(ATrackerGmailMessage::extractMessagePartText)
        .orElseGet(this::getDefaultMessageBodyText)
        .map(Base64::decodeBase64)
        .map(String::new);
  }

  private static Optional<String> extractMessagePartText(MessagePart messagePart) {
    return asOptional(messagePart.getBody()).map(MessagePartBody::getData);
  }

  private Optional<String> getDefaultMessageBodyText() {
    return asOptional(getMessagePayload().getBody()).map(MessagePartBody::getData);
  }

  private List<MessagePart> extractAttachmentMessageParts() {
    return getMessageParts().stream()
        .filter(IS_MIXED_MULTIPART)
        .map(MessagePart::getParts)
        .flatMap(List::stream)
        .filter(IS_SUPPORTED_ATTACHMENT)
        .toList();
  }
}
