package com.example.auth.service;

import com.example.auth.dto.*;
import com.example.auth.entity.*;
import com.example.auth.entity.AuthAuditLog.AuditEvent;
import com.example.auth.exception.AuthException;
import com.example.auth.repository.AuthAuditLogRepository;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository           userRepository;
    private final RefreshTokenRepository   refreshTokenRepository;
    private final AuthAuditLogRepository   auditLogRepository;

    public UserListResponse listUsers(String status, String search, int page, int pageSize) {
        List<User> all = userRepository.findAll();

        List<User> filtered = all.stream()
                .filter(u -> status == null
                        || u.getStatus().name().equalsIgnoreCase(status))
                .filter(u -> search == null || search.isBlank()
                        || u.getEmail().toLowerCase().contains(search.toLowerCase())
                        || (u.getName() != null
                            && u.getName().toLowerCase().contains(search.toLowerCase())))
                .toList();

        int total = filtered.size();
        int start = Math.min((page - 1) * pageSize, total);
        int end   = Math.min(start + pageSize, total);

        List<ManagedUserResponse> page_ = filtered.subList(start, end)
                .stream().map(this::toManaged).toList();

        return UserListResponse.builder().users(page_).total(total).build();
    }

    public ManagedUserResponse getById(String id) {
        return toManaged(findUser(id));
    }

    @Transactional
    public ManagedUserResponse invite(InviteUserRequest req, UUID actorId) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new AuthException("conflict",
                    "A user with this email already exists.", 409);

        User user = User.builder()
                .email(req.getEmail())
                .name(req.getName())
                .avatar(buildInitials(req.getName()))
                .department(req.getDepartment())
                .roles(req.getRoles())
                .status(UserStatus.pending)
                .passwordHash("")
                .enabled(false)
                .build();
        userRepository.save(user);

        User actor = userRepository.findById(actorId).orElse(null);
        auditLogRepository.save(AuthAuditLog.builder()
                .user(actor)
                .event(AuditEvent.INVITE_SENT)
                .severity(AuditSeverity.info)
                .actor(actor != null ? actor.getEmail() : null)
                .target(user.getEmail())
                .build());

        return toManaged(user);
    }

    @Transactional
    public ManagedUserResponse updateUser(String id, UpdateUserRequest req, UUID actorId) {
        User user = findUser(id);

        if (req.getName()       != null) user.setName(req.getName());
        if (req.getDepartment() != null) user.setDepartment(req.getDepartment());
        if (req.getRoles()      != null) {
            user.getRoles().clear();
            user.getRoles().addAll(req.getRoles());
        }
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
            if (req.getStatus() == UserStatus.locked)
                user.setLockedUntil(OffsetDateTime.now().plusYears(100));
            else if (req.getStatus() == UserStatus.active)
                user.setLockedUntil(null);
        }
        userRepository.save(user);

        User actor = userRepository.findById(actorId).orElse(null);
        auditLogRepository.save(AuthAuditLog.builder()
                .user(actor)
                .event(AuditEvent.USER_UPDATED)
                .severity(AuditSeverity.info)
                .actor(actor != null ? actor.getEmail() : null)
                .target(user.getEmail())
                .build());

        return toManaged(user);
    }

    @Transactional
    public void deleteUser(String id, UUID actorId) {
        User user = findUser(id);
        refreshTokenRepository.revokeAllByUserId(user.getId());

        User actor = userRepository.findById(actorId).orElse(null);
        auditLogRepository.save(AuthAuditLog.builder()
                .user(actor)
                .event(AuditEvent.USER_DELETED)
                .severity(AuditSeverity.warning)
                .actor(actor != null ? actor.getEmail() : null)
                .target(user.getEmail())
                .build());

        userRepository.delete(user);
    }

    public AdminStatsResponse getAdminStats() {
        long total       = userRepository.count();
        long activeToday = userRepository.findAll().stream()
                .filter(u -> u.getLastLogin() != null
                        && u.getLastLogin().isAfter(OffsetDateTime.now().minusDays(1)))
                .count();
        long newThisWeek = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null
                        && u.getCreatedAt().isAfter(OffsetDateTime.now().minusWeeks(1)))
                .count();

        return AdminStatsResponse.builder()
                .totalUsers(total)
                .activeToday(activeToday)
                .newThisWeek(newThisWeek)
                .systemLoad((int)(Math.random() * 60 + 20))
                .recentActivity(List.of())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUser(String id) {
        try {
            return userRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new AuthException("not_found", "User not found.", 404));
        } catch (IllegalArgumentException e) {
            throw new AuthException("not_found", "User not found.", 404);
        }
    }

    private ManagedUserResponse toManaged(User u) {
        String lastLogin = u.getLastLogin() != null
                ? u.getLastLogin().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : "—";
        return ManagedUserResponse.builder()
                .id(u.getId().toString())
                .name(u.getName())
                .email(u.getEmail())
                .roles(u.getRoles())
                .status(u.getStatus())
                .department(u.getDepartment())
                .joined(u.getJoined())
                .lastLogin(lastLogin)
                .avatar(u.getAvatar())
                .loginCount(u.getLoginCount())
                .build();
    }

    private String buildInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
