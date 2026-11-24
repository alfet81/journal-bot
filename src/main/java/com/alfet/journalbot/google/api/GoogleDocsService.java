package com.alfet.journalbot.google.api;

import module java.base;

import com.alfet.journalbot.exception.ApplicationException;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.Docs.Documents;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.docs.v1.model.ReplaceAllTextRequest;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.SubstringMatchCriteria;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDocsService {

  private final Docs docsApi;

  private Documents getDocumentsApi() {
    return docsApi.documents();
  }

  private static Request getMergeRequest(String placeholder, String text) {
    var criteria = new SubstringMatchCriteria().setText(placeholder).setMatchCase(true);
    var request = new ReplaceAllTextRequest().setContainsText(criteria).setReplaceText(text);
    return new Request().setReplaceAllText(request);
  }

  public Document getDocument(String id) {
    try {
      return getDocumentsApi().get(id).execute();
    } catch (IOException e) {
      throw new ApplicationException("Failed to get document for id: " + id, e);
    }
  }

  public void mergeDocument(@NonNull String documentId, @NonNull Map<String, String> mapping) {
    if (mapping.isEmpty()) {
      return;
    }
    var requests = mapping.entrySet().stream()
        .map(entry -> getMergeRequest(entry.getKey(), entry.getValue()))
        .toList();

    var body = new BatchUpdateDocumentRequest().setRequests(requests);

    try {
      getDocumentsApi().batchUpdate(documentId, body).execute();
    } catch (IOException e) {
      throw new ApplicationException("Failed to update document with id: " + documentId, e);
    }
  }
}
