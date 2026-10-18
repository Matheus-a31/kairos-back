package com.prumo.project.mapper;
import com.prumo.auth.model.User;


import com.prumo.project.model.Project;
import com.prumo.project.model.ProjectMember;
import com.prumo.project.dto.ProjectMemberResponse;
import com.prumo.project.dto.ProjectRequest;
import com.prumo.project.dto.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toEntity(ProjectRequest request);
    
    ProjectResponse toResponse(Project project);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    ProjectMemberResponse toMemberResponse(ProjectMember member);
}


