package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SimplePostRepository extends JpaRepository<SimplePostEntity, Long> {
    @Query("SELECT sp FROM SimplePostEntity sp " +
            "WHERE (sp.postCreator = :postCreator " +
            "OR sp.postCreator IN (SELECT f FROM UserEntity u JOIN u.friends f WHERE u = :postCreator) " +
            "OR sp.postCreator IN (SELECT f FROM UserEntity u JOIN u.friendOf f WHERE u = :postCreator)) " +
            "AND sp.childPlayedGamePost IS NULL " +
            "AND sp.childPollPost IS NULL " +
            "AND sp.postCreator.suspension IS NULL")
    List<SimplePostEntity> findAllByPostCreatorOrFriends(@Param("postCreator") UserEntity postCreator, Pageable pageable);

    @Query("SELECT sp FROM SimplePostEntity sp " +
            "WHERE (sp.postCreator.id = :postCreatorId)" +
            "AND sp.childPlayedGamePost IS NULL " +
            "AND sp.childPollPost IS NULL")
    List<SimplePostEntity> findByPostCreatorId(@Param("postCreatorId") Long userId, Pageable pageable);

    @Query("SELECT sp FROM SimplePostEntity sp " +
            "WHERE SIZE(sp.reports) > 0" +
            "AND sp.childPlayedGamePost IS NULL " +
            "AND sp.childPollPost IS NULL " +
            "ORDER BY SIZE(sp.reports) DESC")
    List<SimplePostEntity> findReportedPosts(PageRequest pageRequest);
}
