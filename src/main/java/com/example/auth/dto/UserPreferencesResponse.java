package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPreferencesResponse {
    private String displayName;
    private String email;        // read-only
    private String department;   // read-only
    private String timezone;
    private String language;
    private boolean emailOnNewLogin;
    private boolean emailOnPasswordChange;
    private boolean emailOnTokenExpiry;
    private boolean compactMode;
    private String theme;
}
