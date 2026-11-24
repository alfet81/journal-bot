package com.alfet.journalbot.message.mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import com.alfet.journalbot.message.domain.EmailMessage;
import com.alfet.journalbot.message.gmail.GmailMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR,
    uses = {EmailMessageMetadataMapper.class, EmailMessageAttachmentMapper.class})
public interface EmailMessageMapper {

  EmailMessage mapEmailMessage(GmailMessage message);
}
