package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.dto.review.ReviewRequest;
import com.gamesUP.gamesUP.dto.review.ReviewResponse;
import com.gamesUP.gamesUP.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/games/{gameId}/reviews")
    public List<ReviewResponse> findByGame(@PathVariable Long gameId) {
        return reviewService.findByGame(gameId);
    }

    @PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
    @PostMapping("/api/users/{userId}/reviews")
    public ResponseEntity<ReviewResponse> create(@PathVariable Long userId, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(userId, request));
    }

    @PreAuthorize("hasRole('ADMIN') or @reviewSecurity.isOwner(#id, principal.id)")
    @PutMapping("/api/reviews/{id}")
    public ReviewResponse update(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return reviewService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN') or @reviewSecurity.isOwner(#id, principal.id)")
    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
