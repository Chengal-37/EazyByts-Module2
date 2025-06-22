package com.stocktrading.service;

import com.stocktrading.dto.UserDTO;
import com.stocktrading.model.User;
import com.stocktrading.payload.request.UserProfileUpdateRequest; // <-- New import needed
import java.util.Optional;

public interface UserService {
    UserDTO registerUser(UserDTO userDTO);
    UserDTO getUserById(Long id);
    UserDTO getUserByUsername(String username); // Already present and good!
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    Optional<User> findByUsername(String username);

    // --- NEW METHOD FOR UPDATING CURRENT USER'S PROFILE ---
    UserDTO updateUserProfileByUsername(String username, UserProfileUpdateRequest request);
    // -------------------------------------------------------
}