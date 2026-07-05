package com.gamesUP.gamesUP.mapper;

import com.gamesUP.gamesUP.dto.wishlist.WishlistItemResponse;
import com.gamesUP.gamesUP.dto.wishlist.WishlistResponse;
import com.gamesUP.gamesUP.model.Wishlist;
import com.gamesUP.gamesUP.model.WishlistItem;
import org.springframework.stereotype.Component;

@Component
public class WishlistMapper {

    public WishlistResponse toResponse(Wishlist wishlist) {
        var items = wishlist.getItems().stream().map(this::toResponse).toList();
        return new WishlistResponse(wishlist.getId(), wishlist.getUser().getId(), items);
    }

    private WishlistItemResponse toResponse(WishlistItem item) {
        return new WishlistItemResponse(item.getGame().getId(), item.getGame().getName(), item.getAddedAt());
    }
}
