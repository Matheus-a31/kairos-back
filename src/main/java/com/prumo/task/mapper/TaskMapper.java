package com.prumo.task.mapper;

import com.prumo.tag.model.Tag;
import com.prumo.task.model.Task;
import com.prumo.tag.dto.TagResponse;
import com.prumo.task.dto.TaskRequest;
import com.prumo.task.dto.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TagResponse toTagResponse(Tag tag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "kanbanColumn", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tags", ignore = true) // Set manually in service
    Task toEntity(TaskRequest request);

    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.name", target = "assigneeName")
    @Mapping(source = "kanbanColumn.id", target = "kanbanColumnId")
    @Mapping(source = "kanbanColumn.name", target = "kanbanColumnName")
    TaskResponse toResponse(Task task);
}
