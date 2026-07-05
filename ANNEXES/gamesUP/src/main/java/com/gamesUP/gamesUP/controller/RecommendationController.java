package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.dto.recommendation.GameRecommendationResponse;
import com.gamesUP.gamesUP.service.RecommendationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
    @GetMapping
    public List<GameRecommendationResponse> recommendations(@PathVariable Long userId) {
        return recommendationService.recommendationsFor(userId);
    }
}
