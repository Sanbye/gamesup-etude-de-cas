package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.dto.review.ReviewRequest;
import com.gamesUP.gamesUP.dto.review.ReviewResponse;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.ReviewMapper;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Review;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.ReviewRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import com.gamesUP.gamesUP.service.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> findByGame(Long gameId) {
        return reviewRepository.findByGameId(gameId).stream().map(reviewMapper::toResponse).toList();
    }

    @Override
    public ReviewResponse create(Long userId, ReviewRequest request) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + userId));
        Game game = gameRepository
                .findById(request.gameId())
                .orElseThrow(() -> new ResourceNotFoundException("Jeu introuvable : " + request.gameId()));
        Review review = Review.builder()
                .user(user)
                .game(game)
                .comment(request.comment())
                .rating(request.rating())
                .build();
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    public ReviewResponse update(Long id, ReviewRequest request) {
        Review review = reviewRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable : " + id));
        Game game = gameRepository
                .findById(request.gameId())
                .orElseThrow(() -> new ResourceNotFoundException("Jeu introuvable : " + request.gameId()));
        review.setGame(game);
        review.setComment(request.comment());
        review.setRating(request.rating());
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Avis introuvable : " + id);
        }
        reviewRepository.deleteById(id);
    }
}
