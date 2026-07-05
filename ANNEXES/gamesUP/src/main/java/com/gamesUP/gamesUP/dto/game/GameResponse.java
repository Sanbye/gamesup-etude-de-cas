package com.gamesUP.gamesUP.dto.game;

import com.gamesUP.gamesUP.dto.author.AuthorResponse;
import com.gamesUP.gamesUP.dto.category.CategoryResponse;
import com.gamesUP.gamesUP.dto.publisher.PublisherResponse;
import java.math.BigDecimal;
import java.util.Set;

public record GameResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer editionYear,
        Integer stockQuantity,
        CategoryResponse category,
        PublisherResponse publisher,
        Set<AuthorResponse> authors) {}
