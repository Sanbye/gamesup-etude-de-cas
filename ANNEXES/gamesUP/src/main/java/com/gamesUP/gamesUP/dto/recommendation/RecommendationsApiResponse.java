package com.gamesUP.gamesUP.dto.recommendation;

import java.util.List;

/** Miroir de la réponse JSON renvoyée par l'API Python ({@code POST /recommendations/}). */
public record RecommendationsApiResponse(List<GameRecommendationResponse> recommendations) {}
