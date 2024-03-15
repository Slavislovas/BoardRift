package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByNameContainingIgnoreCaseOrLastnameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
}
