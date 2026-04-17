package com.example.auth.service;

import com.example.auth.dto.SystemSettingsResponse;
import com.example.auth.dto.UpdateSystemSettingsRequest;
import com.example.auth.entity.AuditSeverity;
import com.example.auth.entity.AuthAuditLog;
import com.example.auth.entity.AuthAuditLog.AuditEvent;
import com.example.auth.entity.SystemSettings;
import com.example.auth.entity.User;
import com.example.auth.repository.AuthAuditLogRepository;
import com.example.auth.repository.SystemSettingsRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SystemSettingsRepository settingsRepository;
    private final AuthAuditLogRepository   auditLogRepository;
    private final UserRepository           userRepository;

    public SystemSettingsResponse get() {
        return toDto(load());
    }

    @Transactional
    public SystemSettingsResponse update(UpdateSystemSettingsRequest req, UUID actorId) {
        SystemSettings s = load();

        if (req.getLockoutThreshold()        != null) s.setLockoutThreshold(req.getLockoutThreshold());
        if (req.getLockoutDurationMinutes()  != null) s.setLockoutDurationMinutes(req.getLockoutDurationMinutes());
        if (req.getAccessTokenTtlSeconds()   != null) s.setAccessTokenTtlSeconds(req.getAccessTokenTtlSeconds());
        if (req.getRefreshTokenTtlDays()     != null) s.setRefreshTokenTtlDays(req.getRefreshTokenTtlDays());
        if (req.getRememberMeTtlDays()       != null) s.setRememberMeTtlDays(req.getRememberMeTtlDays());
        if (req.getRequireMfa()              != null) s.setRequireMfa(req.getRequireMfa());
        if (req.getAllowedDomains()           != null) s.setAllowedDomains(req.getAllowedDomains());
        if (req.getSessionConcurrencyLimit() != null) s.setSessionConcurrencyLimit(req.getSessionConcurrencyLimit());
        if (req.getAuditRetentionDays()      != null) s.setAuditRetentionDays(req.getAuditRetentionDays());
        settingsRepository.save(s);

        User actor = userRepository.findById(actorId).orElse(null);
        auditLogRepository.save(AuthAuditLog.builder()
                .user(actor)
                .event(AuditEvent.SETTINGS_CHANGED)
                .severity(AuditSeverity.warning)
                .actor(actor != null ? actor.getEmail() : null)
                .build());

        return toDto(s);
    }

    private SystemSettings load() {
        return settingsRepository.findById(1L)
                .orElseGet(() -> settingsRepository.save(new SystemSettings()));
    }

    private SystemSettingsResponse toDto(SystemSettings s) {
        return SystemSettingsResponse.builder()
                .lockoutThreshold(s.getLockoutThreshold())
                .lockoutDurationMinutes(s.getLockoutDurationMinutes())
                .accessTokenTtlSeconds(s.getAccessTokenTtlSeconds())
                .refreshTokenTtlDays(s.getRefreshTokenTtlDays())
                .rememberMeTtlDays(s.getRememberMeTtlDays())
                .requireMfa(s.isRequireMfa())
                .allowedDomains(s.getAllowedDomains())
                .sessionConcurrencyLimit(s.getSessionConcurrencyLimit())
                .auditRetentionDays(s.getAuditRetentionDays())
                .build();
    }
}
