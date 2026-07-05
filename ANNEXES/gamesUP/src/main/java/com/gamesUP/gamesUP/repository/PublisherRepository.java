package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Publisher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    Optional<Publisher> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
