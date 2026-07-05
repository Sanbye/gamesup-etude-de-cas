package com.gamesUP.gamesUP.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.dto.game.GameRequest;
import com.gamesUP.gamesUP.dto.game.GameResponse;
import com.gamesUP.gamesUP.dto.game.GameSearchCriteria;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.AuthorMapper;
import com.gamesUP.gamesUP.mapper.CategoryMapper;
import com.gamesUP.gamesUP.mapper.GameMapper;
import com.gamesUP.gamesUP.mapper.PublisherMapper;
import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.model.Category;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import com.gamesUP.gamesUP.repository.CategoryRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private AuthorRepository authorRepository;

    private GameServiceImpl gameService;

    private Category category;
    private Publisher publisher;
    private Game game;

    @BeforeEach
    void setUp() {
        GameMapper gameMapper = new GameMapper(new CategoryMapper(), new PublisherMapper(), new AuthorMapper());
        gameService = new GameServiceImpl(gameRepository, categoryRepository, publisherRepository, authorRepository, gameMapper);
        category = Category.builder().id(1L).name("Stratégie").build();
        publisher = Publisher.builder().id(1L).name("Days of Wonder").build();
        game = Game.builder()
                .id(1L)
                .name("7 Wonders")
                .price(new BigDecimal("39.90"))
                .stockQuantity(10)
                .category(category)
                .publisher(publisher)
                .authors(Set.of())
                .build();
    }

    private GameRequest requestFor(Category category, Publisher publisher) {
        return new GameRequest(
                "7 Wonders", "desc", new BigDecimal("39.90"), 2010, 10, category.getId(), publisher.getId(), Set.of());
    }

    @Test
    void create_whenCategoryMissing_throwsNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.create(requestFor(category, publisher)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenPublisherMissing_throwsNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.create(requestFor(category, publisher)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenAuthorIdInvalid_throwsNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(42L))).thenReturn(List.of());

        GameRequest request = new GameRequest(
                "7 Wonders", "desc", new BigDecimal("39.90"), 2010, 10, 1L, 1L, Set.of(42L));

        assertThatThrownBy(() -> gameService.create(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesGame() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        GameResponse result = gameService.create(requestFor(category, publisher));

        assertThat(result.name()).isEqualTo("7 Wonders");
        assertThat(result.category().name()).isEqualTo("Stratégie");
    }

    @Test
    void create_withAuthors_resolvesAuthorSet() {
        Author author = Author.builder().id(5L).name("Antoine Bauza").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(5L))).thenReturn(List.of(author));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameRequest request = new GameRequest(
                "7 Wonders", "desc", new BigDecimal("39.90"), 2010, 10, 1L, 1L, Set.of(5L));

        GameResponse result = gameService.create(request);

        assertThat(result.authors()).extracting("name").containsExactly("Antoine Bauza");
    }

    @Test
    void findById_whenMissing_throwsNotFound() {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_whenGameMissing_throwsNotFound() {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.update(99L, requestFor(category, publisher)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_updatesFields() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameRequest request = new GameRequest(
                "7 Wonders Duel", "desc", new BigDecimal("29.90"), 2015, 5, 1L, 1L, Set.of());

        GameResponse result = gameService.update(1L, request);

        assertThat(result.name()).isEqualTo("7 Wonders Duel");
        assertThat(result.stockQuantity()).isEqualTo(5);
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        when(gameRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> gameService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_deletesGame() {
        when(gameRepository.existsById(1L)).thenReturn(true);

        gameService.delete(1L);

        verify(gameRepository).deleteById(1L);
    }

    @Test
    void search_delegatesToRepositoryWithSpecification() {
        when(gameRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Game>>any()))
                .thenReturn(List.of(game));

        List<GameResponse> result =
                gameService.search(new GameSearchCriteria("7 Wonders", 1L, 1L, null, null, null, null));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("7 Wonders");
    }
}
