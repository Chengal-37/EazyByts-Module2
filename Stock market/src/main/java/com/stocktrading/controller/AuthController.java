package com.stocktrading.controller;

import com.stocktrading.payload.request.LoginRequest;
import com.stocktrading.payload.request.RegistrationRequest;
import com.stocktrading.payload.response.JwtResponse;
import com.stocktrading.payload.response.MessageResponse; // Already exists, just ensure it's imported!
import com.stocktrading.security.JwtUtils;
import com.stocktrading.service.impl.UserDetailsImpl;
import com.stocktrading.model.User; // Assuming you have your User model
import com.stocktrading.repository.UserRepository; // Assuming you have your UserRepository

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt,
                                                 userDetails.getId(),
                                                 userDetails.getUsername(),
                                                 userDetails.getEmail()));
    }

    // REGISTRATION ENDPOINT
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        // 1. Check if username already exists
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            // Changed from plain String to MessageResponse object
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // 2. Check if email already exists
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            // Changed from plain String to MessageResponse object
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // 3. Create new user's account
        User user = new User(
                registrationRequest.getUsername(),
                registrationRequest.getEmail(),
                passwordEncoder.encode(registrationRequest.getPassword())
        );

        // Set first name and last name if provided (can be null if not sent by frontend)
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());

        // 4. Save the user to the database
        userRepository.save(user);

        // Changed from plain String to MessageResponse object for success
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}