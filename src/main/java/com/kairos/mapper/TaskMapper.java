package com.kairos.mapper;

import com.kairos.domain.Tag;
import com.kairos.domain.Task;
import com.kairos.dto.TagResponse;
import com.kairos.dto.TaskRequest;
import com.kairos.dto.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TagResponse toTagResponse(Tag tag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tags", ignore = true) // Set manually in service
    Task toEntity(TaskRequest request);

    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.name", target = "assigneeName")
    TaskResponse toResponse(Task task);
}
