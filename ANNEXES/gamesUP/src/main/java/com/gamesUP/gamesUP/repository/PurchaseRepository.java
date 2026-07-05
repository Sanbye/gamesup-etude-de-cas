package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Purchase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByUserId(Long userId);
}
