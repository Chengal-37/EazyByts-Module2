package com.stocktrading.controller;

import com.stocktrading.dto.UserDTO; // Assuming this DTO has username, email, etc.
import com.stocktrading.payload.request.UserProfileUpdateRequest; // <-- YOU MIGHT NEED TO CREATE THIS DTO
import com.stocktrading.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; // Needed for getCurrentUserProfile // Needed for getCurrentUserProfile
import org.springframework.web.bind.annotation.*;
import java.security.Principal; // Alternative for getting username in current user endpoints

@RestController
@RequestMapping("/api/users") // <-- IMPORTANT: See CORS warning below!
public class UserController {

    @Autowired
    private UserService userService;

    // Existing endpoint for user registration
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.registerUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // Existing endpoint to get user by ID (e.g., for admin to view any user)
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Existing endpoint to get user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    // Existing endpoint to update user by ID (e.g., for admin to update any user)
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    // Existing endpoint to delete user by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // --- NEW ENDPOINTS FOR CURRENTLY AUTHENTICATED USER'S PROFILE ---

    /**
     * Retrieves the profile of the currently logged-in user.
     * Accessible at GET /api/users/profile
     */
    @GetMapping("/profile") // This will match /api/users/profile exactly
    public ResponseEntity<UserDTO> getCurrentUserProfile(Principal principal) {
        // 'principal' object contains the authenticated user's name (username or email)
        String username = principal.getName(); // This gets the username from the JWT token

        // You need a method in UserService to find a user by username
        // Ensure userService.findByUsername() returns an Optional<UserDTO> or throws NotFoundException
        UserDTO userDTO = userService.getUserByUsername(username); // Assuming this returns UserDTO
        // If your findByUsername returns an Optional, you'd do:
        // UserDTO userDTO = userService.findByUsername(username)
        //                        .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        return ResponseEntity.ok(userDTO);
    }

    /**
     * Updates the profile of the currently logged-in user.
     * Accessible at PUT /api/users/profile
     *
     * IMPORTANT: You need to create a UserProfileUpdateRequest DTO
     * that contains the fields the user can update (e.g., username, email, newPassword).
     * It should NOT typically contain the user's ID as it's implicit from the token.
     */
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateCurrentUserProfile(@Valid @RequestBody UserProfileUpdateRequest request, Principal principal) {
        String username = principal.getName(); // Get the username of the currently logged-in user

        // You'll need a new method in UserService like `updateUserProfileByUsername`
        // This method should find the user by `username` and apply the updates from `request`.
        // It's crucial that this method only updates the *current* user's profile.
        UserDTO updatedUser = userService.updateUserProfileByUsername(username, request);

        return ResponseEntity.ok(updatedUser);
    }
}