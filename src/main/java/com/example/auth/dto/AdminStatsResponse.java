package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long activeToday;
    private long newThisWeek;
    private int systemLoad;
    private List<RecentActivity> recentActivity;

    @Data
    @Builder
    public static class RecentActivity {
        private int id;
        private String user;
        private String action;
        private String time;
        private String status;
    }
}
