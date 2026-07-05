package com.gamesUP.gamesUP.dto.wishlist;

import java.util.List;

public record WishlistResponse(Long id, Long userId, List<WishlistItemResponse> items) {}
