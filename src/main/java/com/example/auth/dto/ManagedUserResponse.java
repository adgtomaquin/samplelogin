package com.example.auth.dto;

import com.example.auth.entity.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManagedUserResponse {
    private String id;
    private String name;
    private String email;
    private Set<String> roles;
    private UserStatus status;
    private String department;
    private OffsetDateTime joined;
    private String lastLogin;   // ISO string or "—"
    private String avatar;
    private int loginCount;
}
