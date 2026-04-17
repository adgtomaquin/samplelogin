package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuditLogListResponse {
    private List<AuditLogResponse> logs;
    private long total;
}
