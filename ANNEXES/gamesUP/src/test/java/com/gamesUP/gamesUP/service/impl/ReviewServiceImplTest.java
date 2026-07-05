package com.gamesUP.gamesUP.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.dto.review.ReviewRequest;
import com.gamesUP.gamesUP.dto.review.ReviewResponse;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.ReviewMapper;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Review;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.ReviewRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    private ReviewServiceImpl reviewService;

    private User user;
    private Game game;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, userRepository, gameRepository, new ReviewMapper());
        user = User.builder().id(1L).firstName("Jean").lastName("Dupont").email("jean@gamesup.test").role(Role.CLIENT).build();
        game = Game.builder().id(1L).name("7 Wonders").build();
    }

    @Test
    void findByGame_returnsMappedReviews() {
        Review review = Review.builder().id(1L).user(user).game(game).comment("Super jeu").rating(5).build();
        when(reviewRepository.findByGameId(1L)).thenReturn(List.of(review));

        List<ReviewResponse> result = reviewService.findByGame(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).rating()).isEqualTo(5);
    }

    @Test
    void create_whenUserMissing_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ReviewRequest request = new ReviewRequest(1L, "Top", 5);

        assertThatThrownBy(() -> reviewService.create(1L, request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenGameMissing_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        ReviewRequest request = new ReviewRequest(1L, "Top", 5);

        assertThatThrownBy(() -> reviewService.create(1L, request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesReview() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse result = reviewService.create(1L, new ReviewRequest(1L, "Top", 5));

        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.gameId()).isEqualTo(1L);
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        when(reviewRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_deletesReview() {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        reviewService.delete(1L);

        verify(reviewRepository).deleteById(1L);
    }
}
