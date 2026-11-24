package com.alfet.journalbot.google.api;

import module java.base;
import static com.alfet.journalbot.google.api.GmailService.MODIFY_MESSAGE_REQUEST;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alfet.journalbot.config.GoogleApiProperties;
import com.alfet.journalbot.exception.ApplicationException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig // Sets up a minimal Spring test context
@EnableConfigurationProperties(GoogleApiProperties.class) // Loads only this properties bean
@TestPropertySource(properties = {
    "google-api.user-id=test@gmail.com"
})
class GmailServiceTest {

  private static final String MESSAGE_ID = "ABC";
  private static final String USER_ID = "test@gmail.com";
  private static final String QUERY = "is:unread";
  private static final String FULL_FORMAT = "FULL";
  private static final String PAGE_TOKEN = "PT";
  private static final String ATTACHMENT_ID = "XYZ";

  private final Gmail gmailApi = mock(Gmail.class, RETURNS_DEEP_STUBS);

  @Autowired
  private GoogleApiProperties googleApiProperties;

  private GmailService gmailService;

  @BeforeEach
  void setup() {
    gmailService = new GmailService(gmailApi, googleApiProperties);
  }

  @Test
  void getMessages_WhenMessagesFoundAndNoIOExceptionIsThrown() throws IOException {
    // Given
    var message = new Message();
    message.setId(MESSAGE_ID);

    var response = new ListMessagesResponse().setMessages(List.of(message));
    response.setNextPageToken(PAGE_TOKEN);

    when(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(isNull())
        .execute()).thenReturn(response);
    when(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(PAGE_TOKEN)
        .execute()).thenReturn(new ListMessagesResponse().setMessages(List.of()));

    when(gmailApi.users().messages().get(USER_ID, MESSAGE_ID).setFormat(FULL_FORMAT).execute())
        .thenReturn(message);

    when(gmailApi.users().messages().modify(USER_ID, MESSAGE_ID, MODIFY_MESSAGE_REQUEST).execute())
        .thenReturn(message);

    // When
    var messages = gmailService.getMessages(QUERY);

    // Then
    assertThat(messages).isNotNull().hasSize(1);

    verify(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(isNull()))
        .execute();

    verify(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(PAGE_TOKEN))
        .execute();

    verify(gmailApi.users().messages().get(USER_ID, MESSAGE_ID).setFormat(FULL_FORMAT)).execute();

    verify(gmailApi.users().messages().modify(USER_ID, MESSAGE_ID, MODIFY_MESSAGE_REQUEST))
        .execute();
  }

  @Test
  void getMessages_WhenNoMessagesFound() throws IOException {
    // Given
    when(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(isNull()).execute())
        .thenReturn(new ListMessagesResponse().setMessages(List.of()));

    // When
    var messages = gmailService.getMessages(QUERY);

    // Then
    assertThat(messages).isNotNull().isEmpty();

    verify(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(isNull())).execute();
  }


  @Test
  void getMessages_WhenListMessagesThrowsIOException() throws IOException {
    // Given
    var causeException = new IOException();

    when(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(any())
        .execute()).thenThrow(causeException);

    // When
    var exception = catchException(() -> gmailService.getMessages(QUERY));

    // Then
    assertThat(exception).isInstanceOf(ApplicationException.class).hasCause(causeException);

    verify(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(any()))
        .execute();
  }

  @Test
  void getMessages_WhenGetMessageDetailsThrowsIOException() throws IOException {
    // Given
    var causeException = new IOException();

    var message = new Message();
    message.setId(MESSAGE_ID);

    var response = new ListMessagesResponse().setMessages(List.of(message));

    when(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(isNull())
        .execute()).thenReturn(response);

    when(gmailApi.users().messages().get(USER_ID, MESSAGE_ID).setFormat(FULL_FORMAT).execute())
        .thenThrow(causeException);

    // When
    var exception = catchException(() -> gmailService.getMessages(QUERY));

    // Then
    assertThat(exception).isInstanceOf(ApplicationException.class).hasCause(causeException);

    verify(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(any()))
        .execute();

    verify(gmailApi.users().messages().get(USER_ID, MESSAGE_ID).setFormat(FULL_FORMAT)).execute();
  }

  @Test
  void getMessages_WhenModifyThrowsIOException() throws IOException {
    // Given

    var causeException = new IOException();

    var message = new Message();
    message.setId(MESSAGE_ID);

    var response = new ListMessagesResponse().setMessages(List.of(message));

    when(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(isNull())
        .execute()).thenReturn(response);

    when(gmailApi.users().messages().get(USER_ID, MESSAGE_ID).setFormat(FULL_FORMAT).execute())
        .thenReturn(message);

    when(gmailApi.users().messages().modify(USER_ID, MESSAGE_ID, MODIFY_MESSAGE_REQUEST).execute())
        .thenThrow(causeException);

    // When
    var exception = catchException(() -> gmailService.getMessages(QUERY));

    // Then
    assertThat(exception).isInstanceOf(ApplicationException.class).hasCause(causeException);

    verify(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(any()))
        .execute();

    verify(gmailApi.users().messages().get(USER_ID, MESSAGE_ID).setFormat(FULL_FORMAT)).execute();

    verify(gmailApi.users().messages().modify(USER_ID, MESSAGE_ID, MODIFY_MESSAGE_REQUEST))
        .execute();
  }

  @Test
  void getMessages_WhenGetMessageDetailsThrowsIllegalArgumentException() throws IOException {
    // Given
    var message = new Message();
    var response = new ListMessagesResponse().setMessages(List.of(message));

    when(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(isNull())
        .execute()).thenReturn(response);

    // When
    var exception = catchException(() -> gmailService.getMessages(QUERY));

    // Then
    assertThat(exception).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("messageId is marked non-null but is null");

    verify(gmailApi.users().messages().list(USER_ID).setQ(QUERY).setPageToken(any()))
        .execute();

    verify(gmailApi.users().messages().get(USER_ID, MESSAGE_ID).setFormat(FULL_FORMAT), never())
        .execute();
  }

  @Test
  void getMessageAttachmentContent_WhenNoIOExceptionIsThrown() throws IOException {
    // Given
    var attachment = new MessagePartBody();
    attachment.setAttachmentId(ATTACHMENT_ID);
    attachment.setData(new String(encodeBase64("TEST".getBytes(StandardCharsets.UTF_8))));

    when(gmailApi.users().messages().attachments().get(USER_ID, MESSAGE_ID, ATTACHMENT_ID)
        .execute()).thenReturn(attachment);

    // When
    var content = gmailService.getMessageAttachmentContent(MESSAGE_ID, ATTACHMENT_ID);

    // Then
    assertThat(content).isNotNull().asString().isEqualTo("TEST");

    verify(gmailApi.users().messages().attachments().get(USER_ID, MESSAGE_ID, ATTACHMENT_ID))
        .execute();
  }

  @Test
  void getMessageAttachmentContent_WhenIOExceptionIsThrown() throws IOException {
    // Given
    var causeException = new IOException();

    when(gmailApi.users().messages().attachments().get(USER_ID, MESSAGE_ID, ATTACHMENT_ID)
        .execute()).thenThrow(causeException);

    // When
    var exception = catchException(() -> gmailService.getMessageAttachmentContent(MESSAGE_ID,
        ATTACHMENT_ID));

    // Then
    assertThat(exception).isInstanceOf(IOException.class);

    verify(gmailApi.users().messages().attachments().get(USER_ID, MESSAGE_ID, ATTACHMENT_ID))
        .execute();
  }

  private static Stream<Arguments> provideGetMessagesAttachmentContentTestArgs() {
    return Stream.of(
        arguments("messageId is null", null, ATTACHMENT_ID,
            "messageId is marked non-null but is null"),
        arguments("attachmentId is null", MESSAGE_ID, null,
            "attachmentId is marked non-null but is null")
    );
  }

  @ParameterizedTest(name = "[(index)] => {0}")
  @MethodSource("provideGetMessagesAttachmentContentTestArgs")
  @DisplayName("getMessageAttachmentContent_WhenIllegalArguments")
  void getMessageAttachmentContent_WhenIllegalArguments(String scenario, String messageId,
      String attachmentId, String expectedErrorMessage) throws IOException {
    // When
    var exception = catchException(() -> gmailService.getMessageAttachmentContent(messageId,
        attachmentId));

    // Then
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(expectedErrorMessage);

    verify(gmailApi.users().messages().attachments().get(USER_ID, MESSAGE_ID, ATTACHMENT_ID),
        never()).execute();
  }
}
