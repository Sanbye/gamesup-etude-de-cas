package com.gamesUP.gamesUP.service.impl;

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
import com.gamesUP.gamesUP.model.PurchaseLine;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.PurchaseRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import com.gamesUP.gamesUP.service.PurchaseService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PurchaseMapper purchaseMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> findAll() {
        return purchaseRepository.findAll().stream().map(purchaseMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> findByUser(Long userId) {
        return purchaseRepository.findByUserId(userId).stream().map(purchaseMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse findById(Long id) {
        return purchaseMapper.toResponse(getOrThrow(id));
    }

    @Override
    public PurchaseResponse create(Long userId, PurchaseCreateRequest request) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + userId));

        Purchase purchase = Purchase.builder().user(user).status(OrderStatus.PENDING).build();
        List<PurchaseLine> lines = new ArrayList<>();
        for (PurchaseLineRequest lineRequest : request.lines()) {
            Game game = gameRepository
                    .findById(lineRequest.gameId())
                    .orElseThrow(() -> new ResourceNotFoundException("Jeu introuvable : " + lineRequest.gameId()));
            if (game.getStockQuantity() < lineRequest.quantity()) {
                throw new BusinessRuleException("Stock insuffisant pour le jeu : " + game.getName());
            }
            game.setStockQuantity(game.getStockQuantity() - lineRequest.quantity());
            lines.add(PurchaseLine.builder()
                    .purchase(purchase)
                    .game(game)
                    .quantity(lineRequest.quantity())
                    .unitPrice(game.getPrice())
                    .build());
        }
        purchase.setLines(lines);
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    @Override
    public PurchaseResponse updateStatus(Long id, PurchaseStatusUpdateRequest request) {
        Purchase purchase = getOrThrow(id);
        purchase.setStatus(request.status());
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    private Purchase getOrThrow(Long id) {
        return purchaseRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable : " + id));
    }
}
