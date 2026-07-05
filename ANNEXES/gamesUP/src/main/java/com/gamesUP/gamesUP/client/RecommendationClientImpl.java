package com.gamesUP.gamesUP.client;

import com.gamesUP.gamesUP.dto.recommendation.RecommendationsApiResponse;
import com.gamesUP.gamesUP.dto.recommendation.UserRecommendationRequest;
import com.gamesUP.gamesUP.dto.recommendation.GameRecommendationResponse;
import com.gamesUP.gamesUP.exception.RecommendationServiceUnavailableException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class RecommendationClientImpl implements RecommendationClient {

    private final RestClient recommendationRestClient;

    @Override
    public List<GameRecommendationResponse> fetchRecommendations(UserRecommendationRequest request) {
        try {
            RecommendationsApiResponse response = recommendationRestClient
                    .post()
                    .uri("/recommendations/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(RecommendationsApiResponse.class);
            return response != null && response.recommendations() != null ? response.recommendations() : List.of();
        } catch (RestClientException e) {
            throw new RecommendationServiceUnavailableException("Le service de recommandation est indisponible", e);
        }
    }
}
