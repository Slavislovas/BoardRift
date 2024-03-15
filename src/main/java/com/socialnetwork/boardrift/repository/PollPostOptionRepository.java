package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.post.PollOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollPostOptionRepository extends JpaRepository<PollOptionEntity, Long> {
}
