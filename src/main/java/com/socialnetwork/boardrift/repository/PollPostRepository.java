package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.poll_post.PollPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollPostRepository extends JpaRepository<PollPostEntity, Long> {
}