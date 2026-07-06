package com.gamesUP.gamesUP.support;

import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.model.Category;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static User user(Long id, Role role) {
        return User.builder()
                .id(id)
                .firstName("Jean")
                .lastName("Dupont")
                .email("user" + id + "@gamesup.test")
                .password("encoded-password")
                .role(role)
                .build();
    }

    public static Category category(Long id) {
        return Category.builder().id(id).name("Stratégie").build();
    }

    public static Publisher publisher(Long id) {
        return Publisher.builder().id(id).name("Days of Wonder").build();
    }

    public static Author author(Long id) {
        return Author.builder().id(id).name("Antoine Bauza").build();
    }

    public static Game game(Long id) {
        return Game.builder()
                .id(id)
                .name("7 Wonders")
                .description("Jeu de cartes de civilisation")
                .price(new BigDecimal("39.90"))
                .editionYear(2010)
                .stockQuantity(10)
                .categories(Set.of(category(1L)))
                .publisher(publisher(1L))
                .authors(new HashSet<>())
                .build();
    }
}
