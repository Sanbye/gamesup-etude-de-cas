package com.gamesUP.gamesUP.dto.purchase;

import com.gamesUP.gamesUP.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseResponse(
        Long id,
        Long userId,
        LocalDateTime orderDate,
        OrderStatus status,
        List<PurchaseLineResponse> lines,
        BigDecimal totalAmount) {}
