package com.gamesUP.gamesUP.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.client.RecommendationClient;
import com.gamesUP.gamesUP.dto.recommendation.GameRecommendationResponse;
import com.gamesUP.gamesUP.dto.recommendation.UserRecommendationRequest;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Purchase;
import com.gamesUP.gamesUP.model.PurchaseLine;
import com.gamesUP.gamesUP.model.Review;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.PurchaseRepository;
import com.gamesUP.gamesUP.repository.ReviewRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RecommendationClient recommendationClient;

    private RecommendationServiceImpl recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationServiceImpl(
                userRepository, purchaseRepository, reviewRepository, gameRepository, recommendationClient);
        ReflectionTestUtils.setField(recommendationService, "defaultImplicitRating", 3.0);
    }

    @Test
    void recommendationsFor_whenUserMissing_throwsNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> recommendationService.recommendationsFor(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void recommendationsFor_buildsRequestFromPurchasesAndReviews() {
        when(userRepository.existsById(1L)).thenReturn(true);

        Game game1 = Game.builder().id(10L).name("7 Wonders").build();
        Game game2 = Game.builder().id(20L).name("Catan").build();
        PurchaseLine line1 = PurchaseLine.builder().game(game1).quantity(1).build();
        Purchase purchase = Purchase.builder().lines(List.of(line1)).build();
        when(purchaseRepository.findByUserId(1L)).thenReturn(List.of(purchase));

        Review review = Review.builder().game(game2).rating(5).build();
        when(reviewRepository.findByUserId(1L)).thenReturn(List.of(review));

        when(recommendationClient.fetchRecommendations(any())).thenReturn(List.of());

        recommendationService.recommendationsFor(1L);

        ArgumentCaptor<UserRecommendationRequest> captor = ArgumentCaptor.forClass(UserRecommendationRequest.class);
        verify(recommendationClient).fetchRecommendations(captor.capture());

        UserRecommendationRequest sentRequest = captor.getValue();
        assertThat(sentRequest.userId()).isEqualTo(1L);
        assertThat(sentRequest.purchases()).hasSize(2);
        assertThat(sentRequest.purchases())
                .anySatisfy(p -> {
                    assertThat(p.gameId()).isEqualTo(10L);
                    assertThat(p.rating()).isEqualTo(3.0);
                })
                .anySatisfy(p -> {
                    assertThat(p.gameId()).isEqualTo(20L);
                    assertThat(p.rating()).isEqualTo(5.0);
                });
    }

    @Test
    void recommendationsFor_whenGameInCatalog_enrichesWithCatalogName() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(purchaseRepository.findByUserId(1L)).thenReturn(List.of());
        when(reviewRepository.findByUserId(1L)).thenReturn(List.of());
        when(recommendationClient.fetchRecommendations(any()))
                .thenReturn(List.of(new GameRecommendationResponse(5L, "Nom brut Python", 0.9)));

        Game catalogGame = Game.builder().id(5L).name("Nom officiel du catalogue").build();
        when(gameRepository.findById(5L)).thenReturn(Optional.of(catalogGame));

        List<GameRecommendationResponse> result = recommendationService.recommendationsFor(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).gameName()).isEqualTo("Nom officiel du catalogue");
        assertThat(result.get(0).score()).isEqualTo(0.9);
    }

    @Test
    void recommendationsFor_whenGameNotInCatalog_returnsClientResponseAsIs() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(purchaseRepository.findByUserId(1L)).thenReturn(List.of());
        when(reviewRepository.findByUserId(1L)).thenReturn(List.of());
        GameRecommendationResponse stubRecommendation = new GameRecommendationResponse(101L, "Pandemic", null);
        when(recommendationClient.fetchRecommendations(any())).thenReturn(List.of(stubRecommendation));
        when(gameRepository.findById(101L)).thenReturn(Optional.empty());

        List<GameRecommendationResponse> result = recommendationService.recommendationsFor(1L);

        assertThat(result).containsExactly(stubRecommendation);
    }
}
