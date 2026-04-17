package com.example.auth.service;

import com.example.auth.dto.UpdateUserPreferencesRequest;
import com.example.auth.dto.UserPreferencesResponse;
import com.example.auth.entity.User;
import com.example.auth.entity.UserPreferences;
import com.example.auth.exception.AuthException;
import com.example.auth.repository.UserPreferencesRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreferencesService {

    private final UserPreferencesRepository prefsRepository;
    private final UserRepository userRepository;

    public UserPreferencesResponse get(UUID userId) {
        User user = findUser(userId);
        UserPreferences prefs = loadOrCreate(user);
        return toDto(prefs, user);
    }

    @Transactional
    public UserPreferencesResponse update(UUID userId, UpdateUserPreferencesRequest req) {
        User user = findUser(userId);
        UserPreferences prefs = loadOrCreate(user);

        if (req.getDisplayName()           != null) user.setName(req.getDisplayName());
        if (req.getTimezone()              != null) prefs.setTimezone(req.getTimezone());
        if (req.getLanguage()              != null) prefs.setLanguage(req.getLanguage());
        if (req.getEmailOnNewLogin()       != null) prefs.setEmailOnNewLogin(req.getEmailOnNewLogin());
        if (req.getEmailOnPasswordChange() != null) prefs.setEmailOnPasswordChange(req.getEmailOnPasswordChange());
        if (req.getEmailOnTokenExpiry()    != null) prefs.setEmailOnTokenExpiry(req.getEmailOnTokenExpiry());
        if (req.getCompactMode()           != null) prefs.setCompactMode(req.getCompactMode());
        if (req.getTheme()                 != null) prefs.setTheme(req.getTheme());

        userRepository.save(user);
        prefsRepository.save(prefs);
        return toDto(prefs, user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AuthException("unauthorized", "User not found.", 401));
    }

    private UserPreferences loadOrCreate(User user) {
        return prefsRepository.findByUser(user)
                .orElseGet(() -> prefsRepository.save(
                        UserPreferences.builder().user(user).build()));
    }

    private UserPreferencesResponse toDto(UserPreferences p, User u) {
        return UserPreferencesResponse.builder()
                .displayName(u.getName())
                .email(u.getEmail())
                .department(u.getDepartment())
                .timezone(p.getTimezone())
                .language(p.getLanguage())
                .emailOnNewLogin(p.isEmailOnNewLogin())
                .emailOnPasswordChange(p.isEmailOnPasswordChange())
                .emailOnTokenExpiry(p.isEmailOnTokenExpiry())
                .compactMode(p.isCompactMode())
                .theme(p.getTheme())
                .build();
    }
}
