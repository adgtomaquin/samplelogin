package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserListResponse {
    private List<ManagedUserResponse> users;
    private int total;
}
