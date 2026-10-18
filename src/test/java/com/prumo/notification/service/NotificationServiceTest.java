package com.prumo.notification.service;

import com.prumo.auth.model.Role;
import com.prumo.auth.model.User;
import com.prumo.auth.repository.UserRepository;
import com.prumo.notification.dto.NotificationResponse;
import com.prumo.notification.model.Notification;
import com.prumo.notification.model.NotificationType;
import com.prumo.notification.repository.NotificationRepository;
import com.prumo.project.model.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationService notificationService;

    private User currentUser;
    private Project project;

    @BeforeEach
    void setUp() {
        currentUser = new User("User 3", "user3@test.com", "encodedPass", Role.MEMBER);
        currentUser.setId(3L);

        project = new Project("Projeto Teste", "Descricao", LocalDate.now(), LocalDate.now().plusDays(10));
        project.setId(1L);
    }

    private void mockAuth() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user3@test.com");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail("user3@test.com")).thenReturn(Optional.of(currentUser));
    }

    @Test
    void shouldGetUserNotifications() {
        mockAuth();

        Notification n1 = new Notification(currentUser, project, "Você foi atribuído à tarefa: Teste", NotificationType.TASK_ASSIGNED);
        n1.setId(1L);

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(n1));

        List<NotificationResponse> result = notificationService.getUserNotifications();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Você foi atribuído à tarefa: Teste", result.get(0).message());
        assertEquals(NotificationType.TASK_ASSIGNED, result.get(0).type());
        assertEquals(1L, result.get(0).projectId());
        assertEquals("Projeto Teste", result.get(0).projectName());
    }

    @Test
    void shouldGetUnreadCount() {
        mockAuth();
        when(notificationRepository.countByUserIdAndIsReadFalse(3L)).thenReturn(3L);

        long count = notificationService.getUnreadCount();

        assertEquals(3L, count);
    }

    @Test
    void shouldMarkAsRead() {
        mockAuth();

        Notification n = new Notification(currentUser, project, "Teste", NotificationType.TASK_UPDATED);
        n.setId(10L);
        assertFalse(n.getIsRead());

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(10L);

        assertTrue(n.getIsRead());
        verify(notificationRepository).save(n);
    }

    @Test
    void shouldThrowWhenMarkingNotificationOfOtherUser() {
        mockAuth();

        User otherUser = new User("Other", "other@test.com", "pass", Role.MEMBER);
        otherUser.setId(99L);

        Notification n = new Notification(otherUser, project, "Teste", NotificationType.TASK_UPDATED);
        n.setId(20L);

        when(notificationRepository.findById(20L)).thenReturn(Optional.of(n));

        assertThrows(IllegalArgumentException.class, () -> notificationService.markAsRead(20L));
    }

    @Test
    void shouldMarkAllAsRead() {
        mockAuth();

        Notification n1 = new Notification(currentUser, project, "N1", NotificationType.TASK_ASSIGNED);
        Notification n2 = new Notification(currentUser, project, "N2", NotificationType.TASK_UPDATED);

        when(notificationRepository.findByUserIdAndIsReadFalse(3L)).thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead();

        assertTrue(n1.getIsRead());
        assertTrue(n2.getIsRead());
        verify(notificationRepository).saveAll(List.of(n1, n2));
    }

    @Test
    void shouldCreateNotification() {
        Notification n = new Notification(currentUser, project, "Notif", NotificationType.GENERAL);
        when(notificationRepository.save(any(Notification.class))).thenReturn(n);

        notificationService.createNotification(currentUser, project, "Notif", NotificationType.GENERAL);

        verify(notificationRepository).save(any(Notification.class));
    }
}
