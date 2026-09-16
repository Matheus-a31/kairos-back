package com.kairos.project.model;
import com.kairos.auth.model.Role;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_invitations")
public class ProjectInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean used;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ProjectInvitation() {}

    public ProjectInvitation(Project project, String email, String token, ProjectRole role) {
        this.project = project;
        this.email = email;
        this.token = token;
        this.role = role;
        this.expiresAt = LocalDateTime.now().plusDays(7);
        this.used = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
    public ProjectRole getRole() { return role; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}


