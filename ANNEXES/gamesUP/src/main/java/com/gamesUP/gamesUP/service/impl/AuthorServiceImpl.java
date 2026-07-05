package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.dto.author.AuthorRequest;
import com.gamesUP.gamesUP.dto.author.AuthorResponse;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.AuthorMapper;
import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import com.gamesUP.gamesUP.service.AuthorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {
        return authorRepository.findAll().stream().map(authorMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorResponse findById(Long id) {
        return authorMapper.toResponse(getOrThrow(id));
    }

    @Override
    public AuthorResponse create(AuthorRequest request) {
        Author author = Author.builder().name(request.name()).build();
        return authorMapper.toResponse(authorRepository.save(author));
    }

    @Override
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = getOrThrow(id);
        author.setName(request.name());
        return authorMapper.toResponse(authorRepository.save(author));
    }

    @Override
    public void delete(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Auteur introuvable : " + id);
        }
        authorRepository.deleteById(id);
    }

    private Author getOrThrow(Long id) {
        return authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Auteur introuvable : " + id));
    }
}
