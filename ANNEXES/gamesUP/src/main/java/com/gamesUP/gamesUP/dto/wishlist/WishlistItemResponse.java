package com.gamesUP.gamesUP.dto.wishlist;

import java.time.LocalDateTime;

public record WishlistItemResponse(Long gameId, String gameName, LocalDateTime addedAt) {}
