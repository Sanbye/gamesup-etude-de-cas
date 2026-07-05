package com.gamesUP.gamesUP.mapper;

import com.gamesUP.gamesUP.dto.category.CategoryResponse;
import com.gamesUP.gamesUP.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
