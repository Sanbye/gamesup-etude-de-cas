package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.client.RecommendationClient;
import com.gamesUP.gamesUP.dto.recommendation.GameRecommendationResponse;
import com.gamesUP.gamesUP.dto.recommendation.UserPurchaseData;
import com.gamesUP.gamesUP.dto.recommendation.UserRecommendationRequest;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.PurchaseRepository;
import com.gamesUP.gamesUP.repository.ReviewRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import com.gamesUP.gamesUP.service.RecommendationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    private final ReviewRepository reviewRepository;
    private final GameRepository gameRepository;
    private final RecommendationClient recommendationClient;

    @Value("${app.recommendation.api.default-rating}")
    private double defaultImplicitRating;

    @Override
    public List<GameRecommendationResponse> recommendationsFor(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Utilisateur introuvable : " + userId);
        }

        Map<Long, Double> ratingByGameId = new LinkedHashMap<>();
        purchaseRepository.findByUserId(userId).stream()
                .flatMap(purchase -> purchase.getLines().stream())
                .forEach(line -> ratingByGameId.putIfAbsent(line.getGame().getId(), defaultImplicitRating));
        reviewRepository
                .findByUserId(userId)
                .forEach(review -> ratingByGameId.put(review.getGame().getId(), review.getRating().doubleValue()));

        List<UserPurchaseData> purchases = ratingByGameId.entrySet().stream()
                .map(entry -> new UserPurchaseData(entry.getKey(), entry.getValue()))
                .toList();

        List<GameRecommendationResponse> recommendations =
                recommendationClient.fetchRecommendations(new UserRecommendationRequest(userId, purchases));

        return recommendations.stream().map(this::enrichWithCatalog).toList();
    }

    private GameRecommendationResponse enrichWithCatalog(GameRecommendationResponse recommendation) {
        return gameRepository
                .findById(recommendation.gameId())
                .map(game -> new GameRecommendationResponse(game.getId(), game.getName(), recommendation.score()))
                .orElse(recommendation);
    }
}
