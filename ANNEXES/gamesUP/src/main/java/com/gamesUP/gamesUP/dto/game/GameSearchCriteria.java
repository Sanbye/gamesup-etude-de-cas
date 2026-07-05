package com.gamesUP.gamesUP.dto.game;

import java.math.BigDecimal;

public record GameSearchCriteria(
        String name,
        Long categoryId,
        Long publisherId,
        Long authorId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock) {}
