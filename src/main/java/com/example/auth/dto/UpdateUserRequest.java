package com.example.auth.dto;

import com.example.auth.entity.UserStatus;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    private String name;
    private String department;
    private UserStatus status;
    private Set<String> roles;
}
