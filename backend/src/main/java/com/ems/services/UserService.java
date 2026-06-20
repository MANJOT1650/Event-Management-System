package com.ems.services;

import com.ems.dto.UpdateProfileRequest;
import com.ems.repositories.UserRepository;
import com.ems.utils.PasswordUtil;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public java.util.List<com.ems.models.User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public boolean deleteUser(int userId) {
        return userRepository.deleteUser(userId);
    }

    public boolean updateUserRole(int userId, String roleName) {
        try {
            com.ems.models.Role role = com.ems.models.Role.valueOf(roleName.toUpperCase());
            return userRepository.updateUserRole(userId, role);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateProfile(int userId, UpdateProfileRequest request) {
        String hashedPassword = null;
        
        // Only hash if password is provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            hashedPassword = PasswordUtil.hashPassword(request.getPassword());
        }
        
        return userRepository.updateUserProfile(
            userId, 
            request.getName(), 
            request.getEmail(), 
            hashedPassword
        );
    }
}
