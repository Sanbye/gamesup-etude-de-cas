package com.gamesUP.gamesUP.mapper;

import com.gamesUP.gamesUP.dto.purchase.PurchaseLineResponse;
import com.gamesUP.gamesUP.dto.purchase.PurchaseResponse;
import com.gamesUP.gamesUP.model.Purchase;
import com.gamesUP.gamesUP.model.PurchaseLine;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PurchaseMapper {

    public PurchaseResponse toResponse(Purchase purchase) {
        List<PurchaseLineResponse> lines = purchase.getLines().stream().map(this::toResponse).toList();
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getUser().getId(),
                purchase.getOrderDate(),
                purchase.getStatus(),
                lines,
                purchase.getTotalAmount());
    }

    private PurchaseLineResponse toResponse(PurchaseLine line) {
        BigDecimal lineTotal = line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
        return new PurchaseLineResponse(
                line.getGame().getId(), line.getGame().getName(), line.getQuantity(), line.getUnitPrice(), lineTotal);
    }
}
