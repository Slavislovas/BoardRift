package com.socialnetwork.boardrift.repository;

import com.socialnetwork.boardrift.repository.model.post.MarketplacePostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketplacePostRepository extends JpaRepository<MarketplacePostEntity, Long> {
}
