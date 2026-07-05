package com.gamesUP.gamesUP.dto.recommendation;

import java.util.List;

public record UserRecommendationRequest(Long userId, List<UserPurchaseData> purchases) {}
