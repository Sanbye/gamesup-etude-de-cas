package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.dto.purchase.PurchaseCreateRequest;
import com.gamesUP.gamesUP.dto.purchase.PurchaseResponse;
import com.gamesUP.gamesUP.dto.purchase.PurchaseStatusUpdateRequest;
import com.gamesUP.gamesUP.service.PurchaseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/api/purchases")
    public List<PurchaseResponse> findAll() {
        return purchaseService.findAll();
    }

    @GetMapping("/api/purchases/{id}")
    public PurchaseResponse findById(@PathVariable Long id) {
        return purchaseService.findById(id);
    }

    @PatchMapping("/api/purchases/{id}/status")
    public PurchaseResponse updateStatus(@PathVariable Long id, @Valid @RequestBody PurchaseStatusUpdateRequest request) {
        return purchaseService.updateStatus(id, request);
    }

    @GetMapping("/api/users/{userId}/purchases")
    public List<PurchaseResponse> findByUser(@PathVariable Long userId) {
        return purchaseService.findByUser(userId);
    }

    @PostMapping("/api/users/{userId}/purchases")
    public ResponseEntity<PurchaseResponse> create(
            @PathVariable Long userId, @Valid @RequestBody PurchaseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.create(userId, request));
    }
}
