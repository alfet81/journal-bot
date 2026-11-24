package com.alfet.journalbot.google.api;

import module java.base;
import static org.apache.commons.codec.binary.Base64.decodeBase64;

import static java.util.Objects.requireNonNullElse;

import com.alfet.journalbot.config.GoogleApiProperties;
import com.alfet.journalbot.exception.ApplicationException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users.Messages;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailService {

  static final ModifyMessageRequest MODIFY_MESSAGE_REQUEST = new ModifyMessageRequest()
      .setRemoveLabelIds(List.of("UNREAD"));
  private static final String MESSAGE_FULL_FORMAT_TYPE = "FULL";

  private final Gmail gmailApi;
  private final GoogleApiProperties googleApiProperties;

  public List<Message> getMessages(String query) {
    var messages = fetchMessages(query);
    List<Message> result = new ArrayList<>();
    for (var message : messages) {
      var messageWithDetails = fetchMessageDetails(message.getId());
      result.add(messageWithDetails);
    }
    return result;
  }

  private List<Message> fetchMessages(String query) {

    List<Message> result = new ArrayList<>();
    String nextPageToken = null;

    do {
      var messagesResponse = fetchMessages(query, nextPageToken);
      List<Message> messages = requireNonNullElse(messagesResponse.getMessages(), List.of());
      logger.atDebug().log("Fetched '{}' message(s) from Gmail and next page token '{}'",
          messages.size(), messagesResponse.getNextPageToken());
      result.addAll(messages);
      nextPageToken = messagesResponse.getNextPageToken();

    } while (nextPageToken != null);

    logger.atInfo().log("Fetched total {} message(s) from Gmail.", result.size());
    return result;
  }

  private ListMessagesResponse fetchMessages(String query, String pageToken) {
    try {
      logger.atDebug().log("Fetching messages from Gmail for query '{}' and next page token '{}'",
          query, pageToken);
      return getMessagesApi().list(getUserId()).setQ(query).setPageToken(pageToken).execute();
    } catch (IOException e) {
      throw new ApplicationException("Failed to fetch email messages from Google API."
          + " See logs for details.", e);
    }
  }

  private Message fetchMessageDetails(@NonNull String messageId) {
    try {
      logger.atDebug().log("Fetching details for message with id '{}'", messageId);
      var message = getMessagesApi().get(getUserId(), messageId)
          .setFormat(MESSAGE_FULL_FORMAT_TYPE).execute();

      logger.atDebug().log("Marking message with id '{}' as read", messageId);
      getMessagesApi().modify(getUserId(), messageId, MODIFY_MESSAGE_REQUEST).execute();

      logger.atInfo().log("Fetched details for message id {}", messageId);
      return message;
    } catch (IOException e) {
      throw new ApplicationException("Failed to fetch email message from Google API for id: "
          + messageId, e);
    }
  }

  public byte[] getMessageAttachmentContent(@NonNull String messageId, @NonNull String attachmentId)
      throws IOException {
    logger.atDebug().log("Fetching attachment data for file id '{}' and attachment id '{}'",
        messageId, attachmentId);
    var attachment = getMessagesApi().attachments().get(getUserId(), messageId, attachmentId)
        .execute();
    logger.atInfo().log("Successfully fetched attachment data for message id '{}' "
        + "and attachment id '{}'", messageId, attachmentId);
    return decodeBase64(attachment.getData());
  }


  private Messages getMessagesApi() {
    return gmailApi.users().messages();
  }

  private String getUserId() {
    return googleApiProperties.userId();
  }
}
