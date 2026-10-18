package com.prumo.project.repository;
import com.prumo.project.model.Project;

import com.prumo.project.model.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {
    Optional<ProjectInvitation> findByToken(String token);
    boolean existsByProjectIdAndEmailAndUsedFalse(Long projectId, String email);
    java.util.List<ProjectInvitation> findByEmailAndUsedFalse(String email);
}


