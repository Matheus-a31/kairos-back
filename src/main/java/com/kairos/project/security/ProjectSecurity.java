package com.kairos.project.security;
import com.kairos.project.model.Project;

import com.kairos.project.model.ProjectMember;
import com.kairos.project.model.ProjectRole;
import com.kairos.auth.model.User;
import com.kairos.project.repository.ProjectMemberRepository;
import com.kairos.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("projectSecurity")
public class ProjectSecurity {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectSecurity(ProjectMemberRepository projectMemberRepository, UserRepository userRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    public boolean isManager(Authentication authentication, Long projectId) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        if (userOpt.isEmpty()) return false;

        Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userOpt.get().getId());
        return memberOpt.isPresent() && memberOpt.get().getRole() == ProjectRole.MANAGER;
    }

    public boolean isMemberOrManager(Authentication authentication, Long projectId) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        if (userOpt.isEmpty()) return false;

        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userOpt.get().getId());
    }

    public boolean isSelf(Authentication authentication, Long userId) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }
        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        return userOpt.isPresent() && userOpt.get().getId().equals(userId);
    }
}


