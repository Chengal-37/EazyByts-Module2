package com.stocktrading.service.impl;

import com.stocktrading.dto.UserDTO;
import com.stocktrading.model.User;
import com.stocktrading.payload.request.UserProfileUpdateRequest;
import com.stocktrading.repository.UserRepository;
import com.stocktrading.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        // accountBalance is set by @PrePersist in User model if not explicitly set here

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only update if provided and different
        if (userDTO.getFirstName() != null && !userDTO.getFirstName().isEmpty()) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null && !userDTO.getLastName().isEmpty()) {
            user.setLastName(userDTO.getLastName());
        }
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) { // This password is for update by ID, not profile
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        // Note: This method (updateUser by ID) doesn't explicitly handle username/email changes
        // as per the DTO provided. Be cautious if you want it to.
        // It also does not handle account balance updates directly.

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // --- MODIFIED METHOD IMPLEMENTATION FOR UPDATING CURRENT USER'S PROFILE ---
    @Override
    public UserDTO updateUserProfileByUsername(String username, UserProfileUpdateRequest request) {
        // 1. Find the user by their current username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username)); // Should not happen for authenticated user

        // 2. Update First Name if provided
        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }

        // 3. Update Last Name if provided
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }

        // 4. Update password if a new one is provided
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        // 5. Save the updated user
        User updatedUser = userRepository.save(user);

        // 6. Convert and return the updated DTO
        return convertToDTO(updatedUser);
    }
    // ---------------------------------------------------------------------

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAccountBalance(user.getAccountBalance()); // NEW: Set account balance
        // Do NOT set password in DTO that is returned to frontend for security reasons
        // This DTO is used for display purposes, not for sending password back.
        // If this DTO is also used for registration, the password field
        // might be needed during registration but should be null or ignored on response.
        return dto;
    }
}