package com.alfet.journalbot.config;

import module java.base;

import static com.google.api.services.drive.DriveScopes.DRIVE;
import static com.google.api.services.drive.DriveScopes.DRIVE_FILE;
import static com.google.api.services.drive.DriveScopes.DRIVE_METADATA;
import static com.google.api.services.gmail.GmailScopes.GMAIL_ADDONS_CURRENT_MESSAGE_ACTION;
import static com.google.api.services.gmail.GmailScopes.GMAIL_ADDONS_CURRENT_MESSAGE_METADATA;
import static com.google.api.services.gmail.GmailScopes.GMAIL_ADDONS_CURRENT_MESSAGE_READONLY;
import static com.google.api.services.gmail.GmailScopes.GMAIL_COMPOSE;
import static com.google.api.services.gmail.GmailScopes.GMAIL_LABELS;
import static com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.gmail.Gmail;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GoogleApiConfig {

  private static final String ACCESS_TYPE = "offline";
  private static final LocalServerReceiver LOCAL_SERVER_RECEIVER =
      new LocalServerReceiver.Builder()
          .setPort(8888)
          .build();
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final Set<String> GMAIL_SCOPES = Set.of(GMAIL_LABELS,
      GMAIL_ADDONS_CURRENT_MESSAGE_READONLY, MAIL_GOOGLE_COM, GMAIL_COMPOSE,
      GMAIL_ADDONS_CURRENT_MESSAGE_ACTION, GMAIL_ADDONS_CURRENT_MESSAGE_METADATA);

  private static final Set<String> DRIVE_SCOPES = Set.of(DRIVE, DRIVE_FILE, DRIVE_METADATA);
  private static final Set<String> DOC_SCOPES = Set.of(DocsScopes.DRIVE, DocsScopes.DRIVE_FILE,
      DocsScopes.DOCUMENTS);

  private static List<String> getGoogleApiScopes() {
    return Stream.concat(GMAIL_SCOPES.stream(), Stream.concat(DRIVE_SCOPES.stream(),
        DOC_SCOPES.stream())).toList();
  }

  private final GoogleApiProperties googleApiProperties;


  @Bean
  NetHttpTransport httpTransport() {
    return new NetHttpTransport();
  }

  @Bean
  FileDataStoreFactory fileDataStoreFactory() throws IOException {
    return new FileDataStoreFactory(new File(googleApiProperties.tokenDir()));
  }

  @Bean
  GoogleClientSecrets googleClientSecrets() throws IOException {
    return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(loadCredentialsFile()));
  }

  @Bean
  Credential googleApiCredential() throws IOException {
    var flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport(), JSON_FACTORY,
        googleClientSecrets(), getGoogleApiScopes())
        .setDataStoreFactory(fileDataStoreFactory())
        .setAccessType(ACCESS_TYPE)
        .build();

    return new AuthorizationCodeInstalledApp(flow, LOCAL_SERVER_RECEIVER)
        .authorize(googleApiProperties.userId());
  }

  @Bean
  public Gmail gmailApi() throws IOException {
    return new Gmail.Builder(httpTransport(), JSON_FACTORY, googleApiCredential())
        .setApplicationName(googleApiProperties.appName())
        .build();
  }

  @Bean
  public Drive driveApi() throws IOException {
    return new Drive.Builder(httpTransport(), JSON_FACTORY, googleApiCredential())
        .setApplicationName(googleApiProperties.appName())
        .build();
  }

  @Bean
  public Docs docsApi() throws IOException {
    return new Docs.Builder(httpTransport(), JSON_FACTORY, googleApiCredential())
        .setApplicationName(googleApiProperties.appName())
        .build();
  }

  private InputStream loadCredentialsFile() throws FileNotFoundException {
    var resource = GoogleApiConfig.class.getResourceAsStream(googleApiProperties.credentialsFile());
    if (resource == null) {
      throw new FileNotFoundException("Resource not found: " +
          googleApiProperties.credentialsFile());
    }
    return resource;
  }
}
