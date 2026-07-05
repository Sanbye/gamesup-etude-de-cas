package com.gamesUP.gamesUP.dto.purchase;

import com.gamesUP.gamesUP.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record PurchaseStatusUpdateRequest(@NotNull OrderStatus status) {}
