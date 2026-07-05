package com.gamesUP.gamesUP.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.dto.publisher.PublisherRequest;
import com.gamesUP.gamesUP.dto.publisher.PublisherResponse;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.PublisherMapper;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublisherServiceImplTest {

    @Mock
    private PublisherRepository publisherRepository;

    private PublisherServiceImpl publisherService;

    private Publisher publisher;

    @BeforeEach
    void setUp() {
        publisherService = new PublisherServiceImpl(publisherRepository, new PublisherMapper());
        publisher = Publisher.builder().id(1L).name("Days of Wonder").build();
    }

    @Test
    void findAll_returnsMappedPublishers() {
        when(publisherRepository.findAll()).thenReturn(List.of(publisher));

        List<PublisherResponse> result = publisherService.findAll();

        assertThat(result).extracting(PublisherResponse::name).containsExactly("Days of Wonder");
    }

    @Test
    void findById_whenMissing_throwsNotFound() {
        when(publisherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publisherService.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesPublisher() {
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher);

        PublisherResponse result = publisherService.create(new PublisherRequest("Days of Wonder"));

        assertThat(result.name()).isEqualTo("Days of Wonder");
    }

    @Test
    void update_whenExists_updatesName() {
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(publisherRepository.save(any(Publisher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PublisherResponse result = publisherService.update(1L, new PublisherRequest("Asmodee"));

        assertThat(result.name()).isEqualTo("Asmodee");
    }

    @Test
    void delete_whenExists_deletesPublisher() {
        when(publisherRepository.existsById(1L)).thenReturn(true);

        publisherService.delete(1L);

        verify(publisherRepository).deleteById(1L);
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        when(publisherRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> publisherService.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
