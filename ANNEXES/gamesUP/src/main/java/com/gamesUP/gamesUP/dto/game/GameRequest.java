package com.gamesUP.gamesUP.dto.game;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;

public record GameRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        Integer editionYear,
        @NotNull @Min(0) Integer stockQuantity,
        @NotNull Long categoryId,
        @NotNull Long publisherId,
        Set<Long> authorIds) {}
