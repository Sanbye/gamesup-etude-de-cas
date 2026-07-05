package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.recommendation.GameRecommendationResponse;
import java.util.List;

public interface RecommendationService {

    List<GameRecommendationResponse> recommendationsFor(Long userId);
}
