package com.ems.controllers;

import com.ems.dto.UpdateProfileRequest;
import com.ems.services.UserService;
import io.javalin.http.Context;

public class UserController {
    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public void getAllUsers(Context ctx) {
        ctx.json(userService.getAllUsers());
    }

    public void deleteUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (userService.deleteUser(id)) {
            ctx.status(200).json("{\"message\": \"User deleted successfully\"}");
        } else {
            ctx.status(404).json("{\"message\": \"User not found or error deleting\"}");
        }
    }

    public void updateUserRole(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        java.util.Map<String, String> body = ctx.bodyAsClass(java.util.Map.class);
        String role = body.get("role");
        if (userService.updateUserRole(id, role)) {
            ctx.status(200).json("{\"message\": \"User role updated successfully\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Failed to update role\"}");
        }
    }

    public void updateProfile(Context ctx) {
        Integer userId = ctx.attribute("userId");
        if (userId == null) {
            ctx.status(401).json("{\"message\": \"Unauthorized\"}");
            return;
        }

        UpdateProfileRequest request = ctx.bodyAsClass(UpdateProfileRequest.class);
        
        if (request.getName() == null || request.getName().trim().isEmpty() ||
            request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            ctx.status(400).json("{\"message\": \"Name and Email are required\"}");
            return;
        }

        boolean success = userService.updateProfile(userId, request);
        
        if (success) {
            ctx.status(200).json("{\"message\": \"Profile updated successfully\"}");
        } else {
            ctx.status(500).json("{\"message\": \"Failed to update profile. Email might already exist.\"}");
        }
    }
}
