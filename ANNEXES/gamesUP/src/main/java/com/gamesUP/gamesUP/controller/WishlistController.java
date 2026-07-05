package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.dto.wishlist.WishlistAddItemRequest;
import com.gamesUP.gamesUP.dto.wishlist.WishlistResponse;
import com.gamesUP.gamesUP.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/users/{userId}/wishlist")
@RequiredArgsConstructor
@PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public WishlistResponse findByUser(@PathVariable Long userId) {
        return wishlistService.findByUser(userId);
    }

    @PostMapping("/items")
    public WishlistResponse addItem(@PathVariable Long userId, @Valid @RequestBody WishlistAddItemRequest request) {
        return wishlistService.addItem(userId, request);
    }

    @DeleteMapping("/items/{gameId}")
    public ResponseEntity<WishlistResponse> removeItem(@PathVariable Long userId, @PathVariable Long gameId) {
        return ResponseEntity.ok(wishlistService.removeItem(userId, gameId));
    }
}
