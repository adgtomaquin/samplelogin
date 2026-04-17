package com.example.auth.dto;

import lombok.Data;

@Data
public class UpdateUserPreferencesRequest {
    private String displayName;
    private String timezone;
    private String language;
    private Boolean emailOnNewLogin;
    private Boolean emailOnPasswordChange;
    private Boolean emailOnTokenExpiry;
    private Boolean compactMode;
    private String theme;
}
