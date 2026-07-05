package com.gamesUP.gamesUP.mapper;

import com.gamesUP.gamesUP.dto.review.ReviewResponse;
import com.gamesUP.gamesUP.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getGame().getId(),
                review.getComment(),
                review.getRating(),
                review.getCreatedAt());
    }
}
