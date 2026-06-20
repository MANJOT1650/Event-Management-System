package com.ems.controllers;

import com.ems.services.DashboardService;
import io.javalin.http.Context;

public class DashboardController {
    private DashboardService dashboardService;

    public DashboardController() {
        this.dashboardService = new DashboardService();
    }

    public void getAuditLogs(Context ctx) {
        ctx.json(dashboardService.getAuditLogs());
    }

    public void getEventStatistics(Context ctx) {
        ctx.json(dashboardService.getEventStatistics());
    }

    public void getParticipantHistory(Context ctx) {
        Integer userId = ctx.attribute("userId");
        if (userId != null) {
            ctx.json(dashboardService.getParticipantHistory(userId));
        } else {
            ctx.status(401).json("{\"message\": \"Unauthorized\"}");
        }
    }
}
