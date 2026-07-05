package com.gamesUP.gamesUP.dto.purchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PurchaseCreateRequest(@NotEmpty List<@Valid PurchaseLineRequest> lines) {}
