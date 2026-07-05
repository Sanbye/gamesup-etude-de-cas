package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.purchase.PurchaseCreateRequest;
import com.gamesUP.gamesUP.dto.purchase.PurchaseResponse;
import com.gamesUP.gamesUP.dto.purchase.PurchaseStatusUpdateRequest;
import java.util.List;

public interface PurchaseService {

    List<PurchaseResponse> findAll();

    List<PurchaseResponse> findByUser(Long userId);

    PurchaseResponse findById(Long id);

    PurchaseResponse create(Long userId, PurchaseCreateRequest request);

    PurchaseResponse updateStatus(Long id, PurchaseStatusUpdateRequest request);
}
