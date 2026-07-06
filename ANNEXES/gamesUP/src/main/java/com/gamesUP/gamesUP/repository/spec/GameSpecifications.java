package com.gamesUP.gamesUP.repository.spec;

import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.model.Category;
import com.gamesUP.gamesUP.model.Game;
import jakarta.persistence.criteria.Join;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class GameSpecifications {

    private GameSpecifications() {}

    public static Specification<Game> nameContains(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Game> hasCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Game, Category> categories = root.join("categories");
            return cb.equal(categories.get("id"), categoryId);
        };
    }

    public static Specification<Game> hasPublisher(Long publisherId) {
        if (publisherId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("publisher").get("id"), publisherId);
    }

    public static Specification<Game> hasAuthor(Long authorId) {
        if (authorId == null) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Game, Author> authors = root.join("authors");
            return cb.equal(authors.get("id"), authorId);
        };
    }

    public static Specification<Game> priceGreaterOrEqual(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Game> priceLessOrEqual(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Game> inStock(Boolean inStock) {
        if (inStock == null || !inStock) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThan(root.get("stockQuantity"), 0);
    }
}
