package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.dto.user.UserRegisterRequest;
import com.gamesUP.gamesUP.dto.user.UserResponse;
import com.gamesUP.gamesUP.dto.user.UserRoleUpdateRequest;
import com.gamesUP.gamesUP.dto.user.UserUpdateRequest;
import com.gamesUP.gamesUP.exception.BusinessRuleException;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.UserMapper;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.model.Wishlist;
import com.gamesUP.gamesUP.repository.PurchaseRepository;
import com.gamesUP.gamesUP.repository.ReviewRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import com.gamesUP.gamesUP.repository.WishlistRepository;
import com.gamesUP.gamesUP.security.PasswordHasher;
import com.gamesUP.gamesUP.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final PurchaseRepository purchaseRepository;
    private final ReviewRepository reviewRepository;
    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userMapper.toResponse(getOrThrow(id));
    }

    @Override
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Un compte existe déjà avec cet email");
        }
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordHasher.hash(request.password()))
                .role(Role.CLIENT)
                .build();
        User saved = userRepository.save(user);
        wishlistRepository.save(Wishlist.builder().user(saved).build());
        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getOrThrow(id);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateRole(Long id, UserRoleUpdateRequest request) {
        User user = getOrThrow(id);
        user.setRole(request.role());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        User user = getOrThrow(id);
        if (!purchaseRepository.findByUserId(id).isEmpty()) {
            throw new BusinessRuleException("Impossible de supprimer un utilisateur ayant des commandes");
        }
        reviewRepository.findByUserId(id).forEach(review -> reviewRepository.deleteById(review.getId()));
        wishlistRepository.findByUserId(id).ifPresent(wishlist -> wishlistRepository.deleteById(wishlist.getId()));
        userRepository.delete(user);
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }
}
