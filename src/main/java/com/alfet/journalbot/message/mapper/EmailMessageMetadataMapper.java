package com.alfet.journalbot.message.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.alfet.journalbot.message.domain.EmailMessageMetadata;
import com.alfet.journalbot.message.gmail.GmailMessageMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = SPRING)
public interface EmailMessageMetadataMapper {
  EmailMessageMetadata mapEmailMessageMetadata(GmailMessageMetadata gmailMessageMetadata);
}
