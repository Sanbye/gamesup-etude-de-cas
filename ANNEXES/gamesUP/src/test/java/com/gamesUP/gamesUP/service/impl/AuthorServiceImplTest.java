package com.gamesUP.gamesUP.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.dto.author.AuthorRequest;
import com.gamesUP.gamesUP.dto.author.AuthorResponse;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.AuthorMapper;
import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    private AuthorServiceImpl authorService;

    private Author author;

    @BeforeEach
    void setUp() {
        authorService = new AuthorServiceImpl(authorRepository, new AuthorMapper());
        author = Author.builder().id(1L).name("Antoine Bauza").build();
    }

    @Test
    void findAll_returnsMappedAuthors() {
        when(authorRepository.findAll()).thenReturn(List.of(author));

        List<AuthorResponse> result = authorService.findAll();

        assertThat(result).extracting(AuthorResponse::name).containsExactly("Antoine Bauza");
    }

    @Test
    void findById_whenMissing_throwsNotFound() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesAuthor() {
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        AuthorResponse result = authorService.create(new AuthorRequest("Antoine Bauza"));

        assertThat(result.name()).isEqualTo("Antoine Bauza");
    }

    @Test
    void update_whenExists_updatesName() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthorResponse result = authorService.update(1L, new AuthorRequest("Bruno Cathala"));

        assertThat(result.name()).isEqualTo("Bruno Cathala");
    }

    @Test
    void delete_whenExists_deletesAuthor() {
        when(authorRepository.existsById(1L)).thenReturn(true);

        authorService.delete(1L);

        verify(authorRepository).deleteById(1L);
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        when(authorRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> authorService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
