package com.prumo.project.security;
import com.prumo.project.model.Project;

import com.prumo.project.model.ProjectMember;
import com.prumo.project.model.ProjectRole;
import com.prumo.auth.model.User;
import com.prumo.project.repository.ProjectMemberRepository;
import com.prumo.auth.repository.UserRepository;
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

        User user = userOpt.get();
        if (user.getRole() == com.prumo.auth.model.Role.ADMIN || user.getRole() == com.prumo.auth.model.Role.MANAGER) {
            return true;
        }

        Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId());
        return memberOpt.isPresent() && memberOpt.get().getRole() == ProjectRole.MANAGER;
    }

    public boolean isMemberOrManager(Authentication authentication, Long projectId) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        if (user.getRole() == com.prumo.auth.model.Role.ADMIN || user.getRole() == com.prumo.auth.model.Role.MANAGER) {
            return true;
        }

        return projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId());
    }

    public boolean isSelf(Authentication authentication, Long userId) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }
        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        return userOpt.isPresent() && userOpt.get().getId().equals(userId);
    }
}


