package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.dto.wishlist.WishlistAddItemRequest;
import com.gamesUP.gamesUP.dto.wishlist.WishlistResponse;
import com.gamesUP.gamesUP.exception.BusinessRuleException;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.WishlistMapper;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Wishlist;
import com.gamesUP.gamesUP.model.WishlistItem;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.WishlistRepository;
import com.gamesUP.gamesUP.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final GameRepository gameRepository;
    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse findByUser(Long userId) {
        return wishlistMapper.toResponse(getOrThrow(userId));
    }

    @Override
    public WishlistResponse addItem(Long userId, WishlistAddItemRequest request) {
        Wishlist wishlist = getOrThrow(userId);
        boolean alreadyPresent =
                wishlist.getItems().stream().anyMatch(item -> item.getGame().getId().equals(request.gameId()));
        if (alreadyPresent) {
            throw new BusinessRuleException("Ce jeu est déjà dans la liste de souhaits");
        }
        Game game = gameRepository
                .findById(request.gameId())
                .orElseThrow(() -> new ResourceNotFoundException("Jeu introuvable : " + request.gameId()));
        wishlist.getItems().add(WishlistItem.builder().wishlist(wishlist).game(game).build());
        return wishlistMapper.toResponse(wishlistRepository.save(wishlist));
    }

    @Override
    public WishlistResponse removeItem(Long userId, Long gameId) {
        Wishlist wishlist = getOrThrow(userId);
        boolean removed = wishlist.getItems().removeIf(item -> item.getGame().getId().equals(gameId));
        if (!removed) {
            throw new ResourceNotFoundException("Ce jeu n'est pas dans la liste de souhaits");
        }
        return wishlistMapper.toResponse(wishlistRepository.save(wishlist));
    }

    private Wishlist getOrThrow(Long userId) {
        return wishlistRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist introuvable pour l'utilisateur : " + userId));
    }
}
