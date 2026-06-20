package com.ems.services;

import com.ems.dto.AuthResponse;
import com.ems.dto.LoginRequest;
import com.ems.dto.RegisterRequest;
import com.ems.models.Role;
import com.ems.models.User;
import com.ems.repositories.AuthRepository;
import com.ems.utils.JwtUtil;
import com.ems.utils.PasswordUtil;

public class AuthService {
    private AuthRepository authRepository;

    public AuthService() {
        this.authRepository = new AuthRepository();
    }

    public AuthResponse login(LoginRequest request) {
        User user = authRepository.getUserByEmail(request.getEmail());
        if (user != null && PasswordUtil.checkPassword(request.getPassword(), user.getPasswordHash())) {
            String token = JwtUtil.generateToken(user.getUserId(), user.getRole().name());
            return new AuthResponse(token, user.getUserId(), user.getRole().name(), user.getName());
        }
        return null;
    }

    public boolean register(RegisterRequest request) {
        // Validate uniqueness
        if (authRepository.getUserByEmail(request.getEmail()) != null) {
            return false; // Email already exists
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(PasswordUtil.hashPassword(request.getPassword()));
        
        try {
            newUser.setRole(Role.valueOf(request.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            newUser.setRole(Role.PARTICIPANT); // Default fallback
        }

        return authRepository.createUser(newUser);
    }
}
