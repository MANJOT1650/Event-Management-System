package com.ems.controllers;

import com.ems.dto.AuthResponse;
import com.ems.dto.LoginRequest;
import com.ems.dto.RegisterRequest;
import com.ems.services.AuthService;
import io.javalin.http.Context;

public class AuthController {
    private AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    public void login(Context ctx) {
        LoginRequest request = ctx.bodyAsClass(LoginRequest.class);
        AuthResponse response = authService.login(request);
        
        if (response != null) {
            ctx.status(200).json(response);
        } else {
            ctx.status(401).json("{\"message\": \"Invalid credentials\"}");
        }
    }

    public void register(Context ctx) {
        RegisterRequest request = ctx.bodyAsClass(RegisterRequest.class);
        boolean success = authService.register(request);
        
        if (success) {
            ctx.status(201).json("{\"message\": \"Registration successful\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Email already exists or invalid role\"}");
        }
    }
}
