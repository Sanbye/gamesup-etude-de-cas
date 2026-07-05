package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.review.ReviewRequest;
import com.gamesUP.gamesUP.dto.review.ReviewResponse;
import java.util.List;

public interface ReviewService {

    List<ReviewResponse> findByGame(Long gameId);

    ReviewResponse create(Long userId, ReviewRequest request);

    ReviewResponse update(Long id, ReviewRequest request);

    void delete(Long id);
}
