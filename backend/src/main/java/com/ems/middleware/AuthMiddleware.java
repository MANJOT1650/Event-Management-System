package com.ems.middleware;

import com.ems.utils.JwtUtil;
import io.javalin.http.Context;
import io.javalin.security.RouteRole;
import io.jsonwebtoken.Claims;

import java.util.Set;

public class AuthMiddleware {

    public enum AppRole implements RouteRole {
        ANYONE, PARTICIPANT, COORDINATOR, ADMIN
    }

    public static void manageAccess(Context ctx) {
        Set<RouteRole> permittedRoles = ctx.routeRoles();
        if (permittedRoles.isEmpty() || permittedRoles.contains(AppRole.ANYONE)) {
            return;
        }

        // Bypass auth for preflight CORS requests
        if ("OPTIONS".equals(ctx.method().name())) {
            return;
        }

        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new io.javalin.http.UnauthorizedResponse("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        Claims claims = JwtUtil.validateToken(token);

        if (claims == null) {
            throw new io.javalin.http.UnauthorizedResponse("Unauthorized / Expired token");
        }

        String userRoleStr = claims.get("role", String.class);
        AppRole userRole = AppRole.valueOf(userRoleStr);

        if (permittedRoles.contains(userRole)) {
            // Also set user info in context for controllers to use
            ctx.attribute("userId", Integer.parseInt(claims.getSubject()));
            ctx.attribute("userRole", userRole);
        } else {
            throw new io.javalin.http.ForbiddenResponse("Forbidden: Role mismatch");
        }
    }
}
