package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PlayedGamePostRepository extends JpaRepository<PlayedGamePostEntity, Long> {
    @Query("SELECT ps FROM PlayedGamePostEntity ps " +
            "WHERE (ps.basePost.postCreator = :postCreator " +
            "OR ps.basePost.postCreator IN (SELECT f FROM UserEntity u JOIN u.friends f WHERE u = :postCreator) " +
            "OR ps.basePost.postCreator IN (SELECT f FROM UserEntity u JOIN u.friendOf f WHERE u = :postCreator)) " +
            "AND ps.basePost.postCreator.suspension IS NULL")
    List<PlayedGamePostEntity> findAllByPostCreatorOrFriends(@Param("postCreator") UserEntity userEntity, Pageable pageable);

    List<PlayedGamePostEntity> findByBasePostPostCreatorId(Long userId, Pageable pageable);

    @Query("SELECT pgp FROM PlayedGamePostEntity pgp WHERE SIZE(pgp.basePost.reports) > 0 ORDER BY SIZE(pgp.basePost.reports) DESC")
    List<PlayedGamePostEntity> findReportedPosts(Pageable pageable);
}
