package com.alfet.journalbot.message.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.alfet.journalbot.message.domain.EmailMessageAttachment;
import com.alfet.journalbot.message.gmail.GmailMessageAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = SPRING)
public interface EmailMessageAttachmentMapper {

  @Mapping(target = "content", ignore = true)
  @Mapping(target = "filePath", ignore = true)
  EmailMessageAttachment mapEmailMessageAttachment(GmailMessageAttachment gmailMessageAttachment);

  List<EmailMessageAttachment> mapEmailMessageAttachments(
      List<GmailMessageAttachment> gmailMessageAttachments);
}
