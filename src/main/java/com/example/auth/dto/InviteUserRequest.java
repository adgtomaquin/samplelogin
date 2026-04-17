package com.example.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class InviteUserRequest {
    @NotBlank private String name;
    @NotBlank @Email private String email;
    @NotBlank private String department;
    @NotEmpty private Set<String> roles;
}
