package com.stocktrading.repository;

import com.stocktrading.model.ERole;
import com.stocktrading.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> { // Role ID is Integer
    Optional<Role> findByName(ERole name); // Find a role by its enum name
}
