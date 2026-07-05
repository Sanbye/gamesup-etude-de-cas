package com.gamesUP.gamesUP.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.dto.purchase.PurchaseCreateRequest;
import com.gamesUP.gamesUP.dto.purchase.PurchaseLineRequest;
import com.gamesUP.gamesUP.dto.purchase.PurchaseResponse;
import com.gamesUP.gamesUP.dto.purchase.PurchaseStatusUpdateRequest;
import com.gamesUP.gamesUP.exception.BusinessRuleException;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.PurchaseMapper;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.OrderStatus;
import com.gamesUP.gamesUP.model.Purchase;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.PurchaseRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    private PurchaseServiceImpl purchaseService;

    private User user;
    private Game game;

    @BeforeEach
    void setUp() {
        purchaseService = new PurchaseServiceImpl(purchaseRepository, userRepository, gameRepository, new PurchaseMapper());
        user = User.builder().id(1L).firstName("Jean").lastName("Dupont").email("jean@gamesup.test").role(Role.CLIENT).build();
        game = Game.builder().id(1L).name("7 Wonders").price(new BigDecimal("39.90")).stockQuantity(5).build();
    }

    @Test
    void create_whenUserMissing_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        PurchaseCreateRequest request = new PurchaseCreateRequest(List.of(new PurchaseLineRequest(1L, 1)));

        assertThatThrownBy(() -> purchaseService.create(1L, request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenStockInsufficient_throwsBusinessRuleException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        PurchaseCreateRequest request = new PurchaseCreateRequest(List.of(new PurchaseLineRequest(1L, 10)));

        assertThatThrownBy(() -> purchaseService.create(1L, request)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void create_decrementsStockAndComputesTotal() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseCreateRequest request = new PurchaseCreateRequest(List.of(new PurchaseLineRequest(1L, 2)));

        PurchaseResponse result = purchaseService.create(1L, request);

        assertThat(game.getStockQuantity()).isEqualTo(3);
        assertThat(result.totalAmount()).isEqualByComparingTo("79.80");
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void updateStatus_whenMissing_throwsNotFound() {
        when(purchaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.updateStatus(99L, new PurchaseStatusUpdateRequest(OrderStatus.PAID)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_updatesStatus() {
        Purchase purchase = Purchase.builder().id(1L).user(user).status(OrderStatus.PENDING).build();
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseResponse result = purchaseService.updateStatus(1L, new PurchaseStatusUpdateRequest(OrderStatus.PAID));

        assertThat(result.status()).isEqualTo(OrderStatus.PAID);
    }
}
