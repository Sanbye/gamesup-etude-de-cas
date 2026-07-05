package com.gamesUP.gamesUP.dto.purchase;

import java.math.BigDecimal;

public record PurchaseLineResponse(
        Long gameId, String gameName, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
