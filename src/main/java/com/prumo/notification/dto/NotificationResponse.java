package com.prumo.notification.dto;

import com.prumo.notification.model.NotificationType;
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
