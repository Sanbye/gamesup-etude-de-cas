package com.gamesUP.gamesUP.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.dto.category.CategoryRequest;
import com.gamesUP.gamesUP.dto.category.CategoryResponse;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.CategoryMapper;
import com.gamesUP.gamesUP.model.Category;
import com.gamesUP.gamesUP.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryMapper categoryMapper;

    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        categoryMapper = new CategoryMapper();
        categoryService = new CategoryServiceImpl(categoryRepository, categoryMapper);
        category = Category.builder().id(1L).name("Stratégie").build();
    }

    @Test
    void findAll_returnsMappedCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponse> result = categoryService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Stratégie");
    }

    @Test
    void findById_whenExists_returnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse result = categoryService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_whenMissing_throwsNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesAndReturnsCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse result = categoryService.create(new CategoryRequest("Stratégie"));

        assertThat(result.name()).isEqualTo("Stratégie");
    }

    @Test
    void update_whenExists_updatesName() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse result = categoryService.update(1L, new CategoryRequest("Ambiance"));

        assertThat(result.name()).isEqualTo("Ambiance");
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_deletesCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }
}
