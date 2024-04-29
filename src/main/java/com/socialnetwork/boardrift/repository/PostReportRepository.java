package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.post.PostReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostReportRepository extends JpaRepository<PostReportEntity, Long> {
}
