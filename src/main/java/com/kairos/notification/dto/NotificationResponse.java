package com.kairos.notification.dto;

import com.kairos.notification.model.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    Long projectId,
    String projectName,
    String message,
    NotificationType type,
    Boolean isRead,
    LocalDateTime createdAt
) {}
