package com.gamesUP.gamesUP.client;

import com.gamesUP.gamesUP.dto.recommendation.GameRecommendationResponse;
import com.gamesUP.gamesUP.dto.recommendation.UserRecommendationRequest;
import java.util.List;

/** Abstraction de l'appel à l'API Python de recommandation (Dependency Inversion : le service métier
 * ne dépend pas directement de RestClient/HTTP). */
public interface RecommendationClient {

    List<GameRecommendationResponse> fetchRecommendations(UserRecommendationRequest request);
}
