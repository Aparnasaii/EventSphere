package com.event.userservice.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.event.userservice.entity.PasswordResetToken;
import com.event.userservice.entity.User;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Delete all tokens for the user in its own short transaction.
     * Using a JPQL DELETE query avoids the SELECT-then-delete that the derived
     * method uses, making it faster and less prone to lock contention.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.user = :user")
    void deleteByUser(@Param("user") User user);
}