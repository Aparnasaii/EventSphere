package com.event.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.event.userservice.entity.*;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRoleAndStatus(Role role, UserStatus status);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
}