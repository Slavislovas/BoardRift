package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.post.PlayedGamePostEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Map;

@Repository
public interface PlayedGamePostRepository extends JpaRepository<PlayedGamePostEntity, Long> {
    @Query("SELECT ps FROM PlayedGamePostEntity ps " +
            "WHERE ps.postCreator = :postCreator " +
            "OR ps.postCreator IN (SELECT f FROM UserEntity u JOIN u.friends f WHERE u = :postCreator) " +
            "OR ps.postCreator IN (SELECT f FROM UserEntity u JOIN u.friendOf f WHERE u = :postCreator)")
    List<PlayedGamePostEntity> findAllByPostCreatorOrFriends(@Param("postCreator") UserEntity userEntity, PageRequest pageRequest);
}
