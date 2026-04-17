package com.example.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth.dto.*;
import com.example.auth.entity.AuthAuditLog;
import com.example.auth.entity.AuthAuditLog.AuditEvent;
import com.example.auth.entity.AuditSeverity;
import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.User;
import com.example.auth.exception.AuthException;
import com.example.auth.repository.AuthAuditLogRepository;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS  = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository           userRepository;
    private final RefreshTokenRepository   refreshTokenRepository;
    private final AuthAuditLogRepository   auditLogRepository;
    private final JwtService               jwtService;
    private final PasswordEncoder          passwordEncoder;

    private final long refreshExpirySeconds     = 604800;
    private final long refreshRememberMeSeconds = 2592000;

    // ── Login ─────────────────────────────────────────────────────────────────

    @Transactional
    public LoginResponse login(LoginRequest request,
                               String ipAddress, String userAgent, String requestId) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException(
                        "invalid_credentials", "Email or password is incorrect.", 401));

        if (isLocked(user)) {
            audit(user, AuditEvent.USER_LOCKED, AuditSeverity.warning,
                  ipAddress, userAgent, requestId);
            throw new AuthException("account_locked",
                    "Account temporarily locked due to repeated failed login attempts.", 423);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user, ipAddress, userAgent, requestId);
            throw new AuthException("invalid_credentials",
                    "Email or password is incorrect.", 401);
        }

        userRepository.resetFailedAttempts(user.getId());
        user.setLastLogin(OffsetDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);

        audit(user, AuditEvent.LOGIN_SUCCESS, AuditSeverity.info,
              ipAddress, userAgent, requestId);

        return buildLoginResponse(user, request.isRememberMe());
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Transactional
    public TokenResponse refresh(RefreshRequest request,
                                  String ipAddress, String userAgent, String requestId) {

        RefreshToken existing = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthException(
                        "refresh_token_expired",
                        "Refresh token has expired. Please log in again.", 401));

        if (existing.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(existing.getUser().getId());
            audit(existing.getUser(), AuditEvent.REPLAY_DETECTED, AuditSeverity.critical,
                  ipAddress, userAgent, requestId);
            throw new AuthException("refresh_token_reuse_detected",
                    "Token reuse detected. All sessions have been revoked.", 401);
        }

        if (existing.getExpiresAt().isBefore(OffsetDateTime.now())) {
            audit(existing.getUser(), AuditEvent.TOKEN_EXPIRED, AuditSeverity.info,
                  ipAddress, userAgent, requestId);
            throw new AuthException("refresh_token_expired",
                    "Refresh token has expired. Please log in again.", 401);
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        audit(user, AuditEvent.TOKEN_REFRESH, AuditSeverity.info,
              ipAddress, userAgent, requestId);

        return buildTokenResponse(user, false);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Transactional
    public void logout(LogoutRequest request, UUID userId,
                       String ipAddress, String userAgent, String requestId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("unauthorized", "User not found.", 401));

        if (request.isLogoutAllDevices()) {
            refreshTokenRepository.revokeAllByUserId(userId);
            audit(user, AuditEvent.LOGOUT_ALL, AuditSeverity.info,
                  ipAddress, userAgent, requestId);
        } else {
            refreshTokenRepository.findByToken(request.getRefreshToken())
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
            audit(user, AuditEvent.LOGOUT, AuditSeverity.info,
                  ipAddress, userAgent, requestId);
        }
    }

    // ── Me ────────────────────────────────────────────────────────────────────

    public UserProfileResponse getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("unauthorized", "User not found.", 401));
        return toProfile(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static UserProfileResponse toProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatar(user.getAvatar())
                .roles(user.getRoles())
                .department(user.getDepartment())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private LoginResponse buildLoginResponse(User user, boolean rememberMe) {
        long refreshExpiry = rememberMe ? refreshRememberMeSeconds : refreshExpirySeconds;
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRoles());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(OffsetDateTime.now().plusSeconds(refreshExpiry))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .refreshExpiresIn(refreshExpiry)
                .user(toProfile(user))
                .build();
    }

    private TokenResponse buildTokenResponse(User user, boolean rememberMe) {
        long refreshExpiry = rememberMe ? refreshRememberMeSeconds : refreshExpirySeconds;
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRoles());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(OffsetDateTime.now().plusSeconds(refreshExpiry))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .refreshExpiresIn(refreshExpiry)
                .build();
    }

    private boolean isLocked(User user) {
        return user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(OffsetDateTime.now());
    }

    private void handleFailedLogin(User user, String ip, String ua, String requestId) {
        userRepository.incrementFailedAttempts(user.getId());
        audit(user, AuditEvent.LOGIN_FAILED, AuditSeverity.warning, ip, ua, requestId);

        int attempts = user.getFailedLoginAttempts() + 1;
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            userRepository.lockUntil(user.getId(),
                    OffsetDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            audit(user, AuditEvent.USER_LOCKED, AuditSeverity.critical, ip, ua, requestId);
        }
    }

    private void audit(User user, AuditEvent event, AuditSeverity severity,
                       String ip, String ua, String requestId) {
        auditLogRepository.save(AuthAuditLog.builder()
                .user(user)
                .event(event)
                .severity(severity)
                .actor(user != null ? user.getEmail() : null)
                .ipAddress(ip)
                .device(ua)   // User-Agent stored as device for display
                .requestId(requestId)
                .build());
    }
}
