package com.alfet.journalbot.atracker.message.processor;

import module java.base;
import static com.alfet.journalbot.csv.CsvUtils.readCsvFile;
import static com.alfet.journalbot.message.type.MessageType.A_TRACKER;
import static com.alfet.journalbot.util.MiscUtils.asOptional;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.alfet.journalbot.atracker.domain.TaskDetails;
import com.alfet.journalbot.config.JournalBotProperties;
import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.message.domain.EmailMessage;
import com.alfet.journalbot.message.domain.EmailMessageAttachment;
import com.alfet.journalbot.message.processor.AbstractEmailMessageProcessor;
import com.alfet.journalbot.message.type.MessageType;
import com.alfet.journalbot.service.JournalManagementService;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component("atrackerEmailMessageProcessor")
public class ATrackerEmailMessageProcessor extends AbstractEmailMessageProcessor {

  private static final String SUBJECT_PREFIX = "Task details";
  private static final String CSV_FILE_EXTENSION = ".csv";

  private final Function<CSVRecord, TaskDetails> taskDetailsFactory;

  public ATrackerEmailMessageProcessor(JournalBotProperties journalBotProperties,
      JournalManagementService journalManagementService,
      Function<CSVRecord, TaskDetails> taskDetailsFactory) {
    super(journalBotProperties, journalManagementService);
    this.taskDetailsFactory = taskDetailsFactory;
  }

  @Override
  protected MessageType getSupportedMessageType() {
    return A_TRACKER;
  }

  @Override
  protected Optional<String> extractDateFromSubject(String subject) {
    if (StringUtils.isEmpty(subject)) {
      return Optional.empty();
    }
    var result = subject.replace(SUBJECT_PREFIX, EMPTY).replaceAll("[()]", EMPTY);
    return asOptional(result);
  }

  @Override
  protected String getBodyText(EmailMessage emailMessage) {
    var csvFileAttachmentContent = getCsvFileAttachmentContent(emailMessage)
        .orElseThrow(() -> new ApplicationException(
            "Could not find CSV file in the list of email attachments or content is missing."));
    var tasks = parseTasksDetailsCsvContent(csvFileAttachmentContent);

    var processingDate = getProcessingDate(emailMessage);

    return tasks.stream()
        .map((var task) -> task.formatted(processingDate))
        .collect(Collectors.joining("\n"));
  }

  private List<TaskDetails> parseTasksDetailsCsvContent(String csvContent) {
    return readCsvFile(new StringReader(csvContent)).stream()
        .map(this::buildTaskDetails)
        .sorted(Comparator.comparing((var task) -> task.getTimeRange().getStartDateTime()))
        .toList();
  }

  private static Optional<String> getCsvFileAttachmentContent(EmailMessage emailMessage) {
    return emailMessage.getAttachments().stream()
        .filter((var attachment) -> attachment.getFileName().endsWith(CSV_FILE_EXTENSION))
        .map(EmailMessageAttachment::getContent)
        .filter(Objects::nonNull)
        .map((var content) -> new String(content, UTF_8))
        .findFirst();
  }

  private TaskDetails buildTaskDetails(CSVRecord csvRecord) {
    return taskDetailsFactory.apply(csvRecord);
  }
}
