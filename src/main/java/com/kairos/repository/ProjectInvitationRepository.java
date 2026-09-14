package com.kairos.repository;

import com.kairos.domain.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {
    Optional<ProjectInvitation> findByToken(String token);
    boolean existsByProjectIdAndEmailAndUsedFalse(Long projectId, String email);
}
