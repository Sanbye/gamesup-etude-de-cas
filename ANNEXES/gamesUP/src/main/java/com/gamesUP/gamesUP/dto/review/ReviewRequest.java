package com.gamesUP.gamesUP.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(@NotNull Long gameId, String comment, @NotNull @Min(1) @Max(5) Integer rating) {}
