package com.gamesUP.gamesUP.mapper;

import com.gamesUP.gamesUP.dto.author.AuthorResponse;
import com.gamesUP.gamesUP.model.Author;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

    public AuthorResponse toResponse(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }
}
