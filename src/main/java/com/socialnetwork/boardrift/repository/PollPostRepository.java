package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PollPostRepository extends JpaRepository<PollPostEntity, Long> {
    @Query("SELECT pp FROM PollPostEntity pp " +
            "WHERE (pp.basePost.postCreator = :postCreator " +
            "OR pp.basePost.postCreator IN (SELECT f FROM UserEntity u JOIN u.friends f WHERE u = :postCreator) " +
            "OR pp.basePost.postCreator IN (SELECT f FROM UserEntity u JOIN u.friendOf f WHERE u = :postCreator)) " +
            "AND pp.basePost.postCreator.suspension IS NULL")
    List<PollPostEntity> findAllByPostCreatorOrFriends(@Param("postCreator") UserEntity userEntity, Pageable pageable);

    List<PollPostEntity> findByBasePostPostCreatorId(Long userId, Pageable pageable);

    @Query("SELECT pp FROM PollPostEntity pp WHERE SIZE(pp.basePost.reports) > 0 ORDER BY SIZE(pp.basePost.reports) DESC")
    List<PollPostEntity> findReportedPosts(PageRequest pageRequest);
}
