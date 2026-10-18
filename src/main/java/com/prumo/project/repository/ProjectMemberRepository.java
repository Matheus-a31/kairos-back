package com.prumo.project.repository;

import com.prumo.project.model.ProjectMember;
import com.prumo.project.model.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    List<ProjectMember> findByProjectIdAndRole(Long projectId, ProjectRole role);
}
