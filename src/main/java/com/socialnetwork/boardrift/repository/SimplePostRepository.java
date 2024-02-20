package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimplePostRepository extends JpaRepository<SimplePostEntity, Long> {
}
