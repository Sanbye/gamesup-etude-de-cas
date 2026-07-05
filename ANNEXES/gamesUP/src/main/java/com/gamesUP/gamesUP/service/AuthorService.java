package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.author.AuthorRequest;
import com.gamesUP.gamesUP.dto.author.AuthorResponse;
import java.util.List;

public interface AuthorService {

    List<AuthorResponse> findAll();

    AuthorResponse findById(Long id);

    AuthorResponse create(AuthorRequest request);

    AuthorResponse update(Long id, AuthorRequest request);

    void delete(Long id);
}
