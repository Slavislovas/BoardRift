package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.post.SimplePostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SimplePostRepository extends JpaRepository<SimplePostEntity, Long> {
    @Query("SELECT sp FROM SimplePostEntity sp " +
            "WHERE (sp.postCreator = :postCreator " +
            "OR sp.postCreator IN (SELECT f FROM UserEntity u JOIN u.friends f WHERE u = :postCreator) " +
            "OR sp.postCreator IN (SELECT f FROM UserEntity u JOIN u.friendOf f WHERE u = :postCreator)) " +
            "AND sp.childPlayedGamePost IS NULL " +
            "AND sp.childMarketplacePost IS NULL " +
            "AND sp.childPollPost IS NULL")
    List<SimplePostEntity> findAllByPostCreatorOrFriends(@Param("postCreator") UserEntity postCreator, Pageable pageable);
}
