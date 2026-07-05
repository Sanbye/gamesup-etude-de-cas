package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Wishlist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByUserId(Long userId);
}
