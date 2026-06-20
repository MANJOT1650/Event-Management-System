package com.ems.services;

import com.ems.models.AuditLog;
import com.ems.repositories.DashboardRepository;

import java.util.List;
import java.util.Map;

public class DashboardService {
    private DashboardRepository dashboardRepository;

    public DashboardService() {
        this.dashboardRepository = new DashboardRepository();
    }

    public List<AuditLog> getAuditLogs() {
        return dashboardRepository.getAuditLogs();
    }

    public List<Map<String, Object>> getEventStatistics() {
        return dashboardRepository.getEventStatistics();
    }

    public List<Map<String, Object>> getParticipantHistory(int participantId) {
        return dashboardRepository.getParticipantHistory(participantId);
    }
}
