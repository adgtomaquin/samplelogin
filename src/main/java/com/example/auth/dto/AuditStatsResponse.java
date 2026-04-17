package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditStatsResponse {
    private long total;
    private long info;
    private long warning;
    private long critical;
}
