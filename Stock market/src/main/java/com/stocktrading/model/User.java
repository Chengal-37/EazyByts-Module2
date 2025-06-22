package com.stocktrading.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data; // Provides @Getter, @Setter, @ToString, @EqualsAndHashCode, @NoArgsConstructor, @AllArgsConstructor

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data // Lombok: Generates getters, setters, toString, equals, hashCode, and a constructor for all fields (excluding static fields)
@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(max = 20, message = "Username must be at most 20 characters")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(max = 120, message = "Password must be at most 120 characters") // Max size for BCrypt hash
    @Column(nullable = false)
    private String password; // Store hashed password

    @NotBlank(message = "Email cannot be blank")
    @Size(max = 50, message = "Email must be at most 50 characters")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", length = 50) // Optional length
    private String firstName;

    @Column(name = "last_name", length = 50) // Optional length
    private String lastName;

    @Column(name = "account_balance", precision = 19, scale = 2)
    private BigDecimal accountBalance = BigDecimal.valueOf(10000.00); // Default starting balance

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Role Management (if using Spring Security with roles) ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // --- Constructors for Spring Security/Registration ---
    // Lombok's @Data generates a constructor for all fields.
    // For specific registration needs without roles/timestamps, you might need a custom one.
    public User() {
        // Default constructor
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password; // This password should be the plain-text one before hashing in service
        this.accountBalance = BigDecimal.valueOf(10000.00); // Set default balance here
    }

    // Callbacks for automatic timestamp management
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (accountBalance == null) { // Ensure default balance is set if not explicitly provided
            accountBalance = BigDecimal.valueOf(10000.00);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}