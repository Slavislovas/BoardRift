package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.repository.model.post.PollPostEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollPostRepository extends JpaRepository<PollPostEntity, Long> {
    @Query("SELECT pp FROM PollPostEntity pp " +
            "WHERE pp.postCreator = :postCreator " +
            "OR pp.postCreator IN (SELECT f FROM UserEntity u JOIN u.friends f WHERE u = :postCreator) " +
            "OR pp.postCreator IN (SELECT f FROM UserEntity u JOIN u.friendOf f WHERE u = :postCreator)")
    List<PollPostEntity> findAllByPostCreatorOrFriends(@Param("postCreator") UserEntity userEntity, PageRequest pageRequest);
}
