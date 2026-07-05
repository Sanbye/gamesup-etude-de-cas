package com.gamesUP.gamesUP.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.dto.wishlist.WishlistAddItemRequest;
import com.gamesUP.gamesUP.dto.wishlist.WishlistResponse;
import com.gamesUP.gamesUP.exception.BusinessRuleException;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.WishlistMapper;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.model.Wishlist;
import com.gamesUP.gamesUP.model.WishlistItem;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.WishlistRepository;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private GameRepository gameRepository;

    private WishlistServiceImpl wishlistService;

    private User user;
    private Game game;
    private Wishlist wishlist;

    @BeforeEach
    void setUp() {
        wishlistService = new WishlistServiceImpl(wishlistRepository, gameRepository, new WishlistMapper());
        user = User.builder().id(1L).firstName("Jean").lastName("Dupont").email("jean@gamesup.test").role(Role.CLIENT).build();
        game = Game.builder().id(1L).name("7 Wonders").build();
        wishlist = Wishlist.builder().id(1L).user(user).items(new ArrayList<>()).build();
    }

    @Test
    void findByUser_whenMissing_throwsNotFound() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.findByUser(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addItem_addsGameToWishlist() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WishlistResponse result = wishlistService.addItem(1L, new WishlistAddItemRequest(1L));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).gameId()).isEqualTo(1L);
    }

    @Test
    void addItem_whenAlreadyPresent_throwsBusinessRuleException() {
        wishlist.getItems().add(WishlistItem.builder().wishlist(wishlist).game(game).build());
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));

        assertThatThrownBy(() -> wishlistService.addItem(1L, new WishlistAddItemRequest(1L)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void removeItem_whenPresent_removesGame() {
        wishlist.getItems().add(WishlistItem.builder().wishlist(wishlist).game(game).build());
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WishlistResponse result = wishlistService.removeItem(1L, 1L);

        assertThat(result.items()).isEmpty();
    }

    @Test
    void removeItem_whenAbsent_throwsNotFound() {
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));

        assertThatThrownBy(() -> wishlistService.removeItem(1L, 1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
