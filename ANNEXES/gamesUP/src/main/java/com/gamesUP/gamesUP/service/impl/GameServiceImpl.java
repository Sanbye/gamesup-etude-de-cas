package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.dto.game.GameRequest;
import com.gamesUP.gamesUP.dto.game.GameResponse;
import com.gamesUP.gamesUP.dto.game.GameSearchCriteria;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.GameMapper;
import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.model.Category;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import com.gamesUP.gamesUP.repository.CategoryRepository;
import com.gamesUP.gamesUP.repository.GameRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import com.gamesUP.gamesUP.repository.spec.GameSpecifications;
import com.gamesUP.gamesUP.service.GameService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final GameMapper gameMapper;

    @Override
    @Transactional(readOnly = true)
    public List<GameResponse> search(GameSearchCriteria criteria) {
        Specification<Game> specification = Specification.where(GameSpecifications.nameContains(criteria.name()))
                .and(GameSpecifications.hasCategory(criteria.categoryId()))
                .and(GameSpecifications.hasPublisher(criteria.publisherId()))
                .and(GameSpecifications.hasAuthor(criteria.authorId()))
                .and(GameSpecifications.priceGreaterOrEqual(criteria.minPrice()))
                .and(GameSpecifications.priceLessOrEqual(criteria.maxPrice()))
                .and(GameSpecifications.inStock(criteria.inStock()));
        return gameRepository.findAll(specification).stream().map(gameMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GameResponse findById(Long id) {
        return gameMapper.toResponse(getOrThrow(id));
    }

    @Override
    public GameResponse create(GameRequest request) {
        Game game = Game.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .editionYear(request.editionYear())
                .stockQuantity(request.stockQuantity())
                .category(getCategoryOrThrow(request.categoryId()))
                .publisher(getPublisherOrThrow(request.publisherId()))
                .authors(resolveAuthors(request.authorIds()))
                .build();
        return gameMapper.toResponse(gameRepository.save(game));
    }

    @Override
    public GameResponse update(Long id, GameRequest request) {
        Game game = getOrThrow(id);
        game.setName(request.name());
        game.setDescription(request.description());
        game.setPrice(request.price());
        game.setEditionYear(request.editionYear());
        game.setStockQuantity(request.stockQuantity());
        game.setCategory(getCategoryOrThrow(request.categoryId()));
        game.setPublisher(getPublisherOrThrow(request.publisherId()));
        game.setAuthors(resolveAuthors(request.authorIds()));
        return gameMapper.toResponse(gameRepository.save(game));
    }

    @Override
    public void delete(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jeu introuvable : " + id);
        }
        gameRepository.deleteById(id);
    }

    private Game getOrThrow(Long id) {
        return gameRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Jeu introuvable : " + id));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
    }

    private Publisher getPublisherOrThrow(Long id) {
        return publisherRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Éditeur introuvable : " + id));
    }

    private Set<Author> resolveAuthors(Set<Long> authorIds) {
        if (CollectionUtils.isEmpty(authorIds)) {
            return new HashSet<>();
        }
        List<Author> found = authorRepository.findAllById(authorIds);
        if (found.size() != authorIds.size()) {
            throw new ResourceNotFoundException("Un ou plusieurs auteurs sont introuvables");
        }
        return new HashSet<>(found);
    }
}
