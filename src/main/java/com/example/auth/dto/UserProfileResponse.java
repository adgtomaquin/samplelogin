package com.example.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private UUID id;
    private String email;
    private String name;
    private String avatar;
    private Set<String> roles;
    private String department;

    @JsonProperty("lastLogin")
    private OffsetDateTime lastLogin;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
