package com.stocktrading.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // Add this if not using @Data directly

@Entity
@Table(name = "roles")
@Data // Lombok: Generates getters, setters, toString, equals, hashCode, and a constructor for all fields
@NoArgsConstructor // Lombok: Generates a no-argument constructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Integer is common for role IDs

    @Enumerated(EnumType.STRING) // Store enum name as String in DB (e.g., "ROLE_USER")
    @Column(length = 20) // Max length for the role name
    private ERole name; // The actual role name (e.g., ROLE_USER, ROLE_ADMIN)

    // Constructor (if not using @Data's all-args constructor)
    public Role(ERole name) {
        this.name = name;
    }
}
