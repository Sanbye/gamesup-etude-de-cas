package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.wishlist.WishlistAddItemRequest;
import com.gamesUP.gamesUP.dto.wishlist.WishlistResponse;

public interface WishlistService {

    WishlistResponse findByUser(Long userId);

    WishlistResponse addItem(Long userId, WishlistAddItemRequest request);

    WishlistResponse removeItem(Long userId, Long gameId);
}
