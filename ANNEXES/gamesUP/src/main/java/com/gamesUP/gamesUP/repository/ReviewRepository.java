package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByGameId(Long gameId);

    List<Review> findByUserId(Long userId);
}
