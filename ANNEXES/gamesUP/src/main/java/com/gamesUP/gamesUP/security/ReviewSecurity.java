package com.gamesUP.gamesUP.security;

import com.gamesUP.gamesUP.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("reviewSecurity")
@RequiredArgsConstructor
public class ReviewSecurity {

    private final ReviewRepository reviewRepository;

    public boolean isOwner(Long reviewId, Long userId) {
        return reviewRepository
                .findById(reviewId)
                .map(review -> review.getUser().getId().equals(userId))
                .orElse(false);
    }
}
