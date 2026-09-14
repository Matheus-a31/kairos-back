package com.kairos.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private TaskStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private TaskStatus newStatus;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public TaskHistory() {}

    public TaskHistory(Task task, User changedBy, TaskStatus oldStatus, TaskStatus newStatus, String comment) {
        this.task = task;
        this.changedBy = changedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.comment = comment;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Task getTask() { return task; }
    public User getChangedBy() { return changedBy; }
    public TaskStatus getOldStatus() { return oldStatus; }
    public TaskStatus getNewStatus() { return newStatus; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
