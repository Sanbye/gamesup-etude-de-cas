package com.gamesUP.gamesUP.dto.purchase;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PurchaseLineRequest(@NotNull Long gameId, @NotNull @Min(1) Integer quantity) {}
