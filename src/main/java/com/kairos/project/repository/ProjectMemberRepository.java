package com.kairos.project.repository;

import com.kairos.project.model.ProjectMember;
import com.kairos.project.model.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    List<ProjectMember> findByProjectIdAndRole(Long projectId, ProjectRole role);
}
