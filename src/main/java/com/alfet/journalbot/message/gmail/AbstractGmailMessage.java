package com.alfet.journalbot.message.gmail;

import static java.util.Objects.requireNonNullElse;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractGmailMessage implements GmailMessage {

  @Getter(AccessLevel.PROTECTED)
  private final Message message;

  protected MessagePart getMessagePayload() {
    return message.getPayload();
  }

  protected List<MessagePart> getMessageParts() {
    return requireNonNullElse(getMessagePayload().getParts(), List.of());
  }

  @Override
  public String getId() {
    return message.getId();
  }

  @Override
  public String getText() {
    return message.getSnippet();
  }

  @Override
  public GmailMessageMetadata getMetadata() {
    return new GmailMessageMetadataImpl(getMessagePayload());
  }
}
