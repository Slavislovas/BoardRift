package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.enumeration.VerificationTokenType;
import com.socialnetwork.boardrift.repository.model.VerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationTokenEntity, String> {
    Optional<VerificationTokenEntity> findByTokenAndType(String token, VerificationTokenType emailVerification);
}
