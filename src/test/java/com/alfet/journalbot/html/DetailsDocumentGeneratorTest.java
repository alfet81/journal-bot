package com.alfet.journalbot.html;

import module java.base;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.alfet.journalbot.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
class DetailsDocumentGeneratorTest {

  @Value("classpath:html/details_document_template.html")
  private Resource detailsDocumentTemplateResource;

  private DetailsDocumentGenerator detailsDocumentGenerator;

  private static Stream<Arguments> provideGenerateTestArgs() {
    return Stream.of(Month.values())
        .map((var month) -> arguments(month, LocalDate.of(2026, month, 1)));
  }

  private String getDocumentTemplate() throws IOException {
    return detailsDocumentTemplateResource.getContentAsString(UTF_8);
  }

  @BeforeEach
  void setUp() {
    detailsDocumentGenerator = new DetailsDocumentGenerator();
    ReflectionTestUtils.setField(detailsDocumentGenerator, "detailsDocumentTemplateResource",
        detailsDocumentTemplateResource);
  }

  @ParameterizedTest(name = "[{index}] => {0}")
  @MethodSource("provideGenerateTestArgs")
  void generate_WhenFullMonth(Month month, LocalDate date) throws IOException {
    // When
    var result = detailsDocumentGenerator.generate(date);

    // Then
    assertThat(result).isNotNull()
        .isNotEqualTo(getDocumentTemplate());
  }

  @Test
  void generate_WhenErrorIsThrown() throws IOException {
    // Given
    var badResource = mock(Resource.class);
    var exception = new IOException();
    when(badResource.getContentAsString(UTF_8)).thenThrow(exception);
    ReflectionTestUtils.setField(detailsDocumentGenerator, "detailsDocumentTemplateResource",
        badResource);

    // When
    assertThatThrownBy(() -> detailsDocumentGenerator.generate(LocalDate.of(2026, 10, 26)))
        .isInstanceOf(ApplicationException.class).hasCause(exception);
  }
}
