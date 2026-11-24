package com.alfet.journalbot.atracker.mapper;

import com.alfet.journalbot.atracker.csv.TaskCsvRecord;
import com.alfet.journalbot.atracker.domain.TaskDetails;
import com.alfet.journalbot.atracker.domain.TimeRange;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING, imports = {TimeRange.class})
public interface TaskDetailsMapper {

  @Mapping(target = "timeRange", expression
      = "java(TimeRange.of(taskCsvRecord.getStartDateTime(), taskCsvRecord.getEndDateTime()))")
  TaskDetails toTaskDetails(TaskCsvRecord taskCsvRecord);
}
