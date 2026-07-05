package com.gamesUP.gamesUP.dto.review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id, Long userId, Long gameId, String comment, Integer rating, LocalDateTime createdAt) {}
