package com.example.auth.service;

import com.example.auth.dto.NotificationListResponse;
import com.example.auth.dto.NotificationResponse;
import com.example.auth.entity.Notification;
import com.example.auth.entity.User;
import com.example.auth.exception.AuthException;
import com.example.auth.repository.NotificationRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationListResponse getForUser(UUID userId, boolean unreadOnly) {
        User user = findUser(userId);
        List<Notification> list = unreadOnly
                ? notificationRepository.findUnreadForUser(user)
                : notificationRepository.findForUser(user);
        long unread = notificationRepository.countUnreadForUser(user);

        List<NotificationResponse> responses = list.stream()
                .map(n -> toDto(n, user)).toList();

        return NotificationListResponse.builder()
                .notifications(responses)
                .unreadCount(unread)
                .build();
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadForUser(findUser(userId));
    }

    @Transactional
    public void markRead(UUID notifId) {
        Notification n = notificationRepository.findById(notifId)
                .orElseThrow(() -> new AuthException("not_found", "Notification not found.", 404));
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void delete(UUID notifId) {
        if (!notificationRepository.existsById(notifId))
            throw new AuthException("not_found", "Notification not found.", 404);
        notificationRepository.deleteById(notifId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AuthException("unauthorized", "User not found.", 401));
    }

    private NotificationResponse toDto(Notification n, User currentUser) {
        String userId = n.getUser() == null ? "all" : n.getUser().getId().toString();
        return NotificationResponse.builder()
                .id(n.getId().toString())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .time(relativeTime(n.getCreatedAt()))
                .read(n.isRead())
                .userId(userId)
                .build();
    }

    private String relativeTime(OffsetDateTime time) {
        if (time == null) return "";
        Duration d = Duration.between(time, OffsetDateTime.now());
        long mins = d.toMinutes();
        if (mins < 1)   return "just now";
        if (mins < 60)  return mins + " min ago";
        long hrs = d.toHours();
        if (hrs  < 24)  return hrs + " hr ago";
        return d.toDays() + " days ago";
    }
}
