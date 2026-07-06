package com.gamesUP.gamesUP.mapper;

import com.gamesUP.gamesUP.dto.author.AuthorResponse;
import com.gamesUP.gamesUP.dto.category.CategoryResponse;
import com.gamesUP.gamesUP.dto.game.GameResponse;
import com.gamesUP.gamesUP.model.Game;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameMapper {

    private final CategoryMapper categoryMapper;
    private final PublisherMapper publisherMapper;
    private final AuthorMapper authorMapper;

    public GameResponse toResponse(Game game) {
        Set<CategoryResponse> categories =
                game.getCategories().stream().map(categoryMapper::toResponse).collect(Collectors.toSet());
        Set<AuthorResponse> authors =
                game.getAuthors().stream().map(authorMapper::toResponse).collect(Collectors.toSet());
        return new GameResponse(
                game.getId(),
                game.getName(),
                game.getDescription(),
                game.getPrice(),
                game.getEditionYear(),
                game.getStockQuantity(),
                categories,
                publisherMapper.toResponse(game.getPublisher()),
                authors);
    }
}
