package com.gamesUP.gamesUP.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.dto.user.UserRegisterRequest;
import com.gamesUP.gamesUP.dto.user.UserResponse;
import com.gamesUP.gamesUP.dto.user.UserRoleUpdateRequest;
import com.gamesUP.gamesUP.dto.user.UserUpdateRequest;
import com.gamesUP.gamesUP.exception.BusinessRuleException;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.UserMapper;
import com.gamesUP.gamesUP.model.Purchase;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.model.Wishlist;
import com.gamesUP.gamesUP.repository.PurchaseRepository;
import com.gamesUP.gamesUP.repository.ReviewRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import com.gamesUP.gamesUP.repository.WishlistRepository;
import com.gamesUP.gamesUP.security.PasswordHasher;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository, wishlistRepository, purchaseRepository, reviewRepository, new UserMapper(), passwordHasher);
        user = User.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean@gamesup.test")
                .password("hashed")
                .role(Role.CLIENT)
                .build();
    }

    @Test
    void register_whenEmailAlreadyUsed_throwsBusinessRuleException() {
        when(userRepository.existsByEmail("jean@gamesup.test")).thenReturn(true);

        UserRegisterRequest request = new UserRegisterRequest("Jean", "Dupont", "jean@gamesup.test", "password123");

        assertThatThrownBy(() -> userService.register(request)).isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_createsUserAsClientWithHashedPasswordAndWishlist() {
        when(userRepository.existsByEmail("jean@gamesup.test")).thenReturn(false);
        when(passwordHasher.hash("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserRegisterRequest request = new UserRegisterRequest("Jean", "Dupont", "jean@gamesup.test", "password123");

        UserResponse result = userService.register(request);

        assertThat(result.role()).isEqualTo(Role.CLIENT);
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void findById_whenMissing_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_updatesProfileFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = userService.update(1L, new UserUpdateRequest("Jeanne", "Dupont", "jeanne@gamesup.test"));

        assertThat(result.firstName()).isEqualTo("Jeanne");
        assertThat(result.email()).isEqualTo("jeanne@gamesup.test");
    }

    @Test
    void updateRole_changesRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = userService.updateRole(1L, new UserRoleUpdateRequest(Role.ADMIN));

        assertThat(result.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void delete_whenUserHasPurchases_throwsBusinessRuleException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(purchaseRepository.findByUserId(1L)).thenReturn(List.of(new Purchase()));

        assertThatThrownBy(() -> userService.delete(1L)).isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void delete_whenNoPurchases_deletesUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(purchaseRepository.findByUserId(1L)).thenReturn(List.of());
        when(reviewRepository.findByUserId(1L)).thenReturn(List.of());
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        userService.delete(1L);

        verify(userRepository).delete(user);
    }
}
