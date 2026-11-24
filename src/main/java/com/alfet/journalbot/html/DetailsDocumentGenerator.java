package com.alfet.journalbot.html;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Objects.requireNonNull;

import com.alfet.journalbot.exception.ApplicationException;
import com.alfet.journalbot.util.MiscUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DetailsDocumentGenerator {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yy");

  private static final Set<DayOfWeek> WEEKEND = EnumSet.of(SATURDAY, SUNDAY);

  private static final String WEEK_COUNTER_HTML = """
      <h3 class="c8">
        <span class="c6">%s -я неделя</span>
      </h3>
      """;

  private static final String WEEKDAY_HEADER_HTML = """
      <h4 class="c4">
        <span class="c5">%s - %s ()</span>
      </h4>
      """;

  private static final String WEEKEND_HEADER_HTML = """
      <h4 class="c4">
        <span class="c7">%s</span>
        <span class="c5">&nbsp;- %s ()</span>
      </h4>
      """;

  private static final String CONTENT_MARKER_HTML = """
      <p class="c1">
        <span>{{%s_marker}}</span>
      </p>
      """;


  @Value("classpath:templates/details_document_template.html")
  private Resource detailsDocumentTemplateResource;


  private static String formatDate(LocalDate date) {
    return DATE_FORMATTER.format(date);
  }

  private static void addWeekCounter(Element element, int weekCounter) {
    var html = WEEK_COUNTER_HTML.formatted(weekCounter);
    element.append(html);
  }

  private static void addDayHeader(Element element, String dayOfWeek, String date,
      boolean weekend) {
    String html = (weekend ? WEEKEND_HEADER_HTML : WEEKDAY_HEADER_HTML);
    html = html.formatted(dayOfWeek, date);
    element.append(html);
  }

  private static void addContentMarker(Element element, LocalDate date) {
    var html = CONTENT_MARKER_HTML.formatted(MiscUtils.formatDate(date));
    element.append(html);
  }

  public String generate(LocalDate date) {

    var htmlDoc = Jsoup.parse(getTemplate());
    var body = requireNonNull(htmlDoc.select("body").first());

    var weekCounter = new AtomicInteger(1);
    int totalMonthDaysCount = date.getMonth().length(date.isLeapYear());

    addWeekCounter(body, weekCounter.get());

    IntStream.rangeClosed(1, totalMonthDaysCount).forEach(currentDay -> {
      var currentDate = LocalDate.of(date.getYear(), date.getMonth(), currentDay);
      var dateAsString = formatDate(currentDate);
      var dayOfWeek = currentDate.getDayOfWeek();
      var rusDayOfWeek = RussianDayOfWeek.getValue(dayOfWeek).orElse("N/A");

      addDayHeader(body, rusDayOfWeek, dateAsString, WEEKEND.contains(dayOfWeek));
      addContentMarker(body, currentDate);

      if (dayOfWeek == SUNDAY && currentDay < totalMonthDaysCount) {
        addWeekCounter(body, weekCounter.addAndGet(1));
      }
    });

    return htmlDoc.toString();
  }

  private String getTemplate() {
    try {
      return detailsDocumentTemplateResource.getContentAsString(UTF_8);
    } catch (IOException e) {
      throw new ApplicationException("Failed to read details document template", e);
    }
  }
}
