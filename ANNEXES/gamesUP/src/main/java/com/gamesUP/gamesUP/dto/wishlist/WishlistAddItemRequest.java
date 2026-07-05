package com.gamesUP.gamesUP.dto.wishlist;

import jakarta.validation.constraints.NotNull;

public record WishlistAddItemRequest(@NotNull Long gameId) {}
