package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.dto.publisher.PublisherRequest;
import com.gamesUP.gamesUP.dto.publisher.PublisherResponse;
import com.gamesUP.gamesUP.exception.ResourceNotFoundException;
import com.gamesUP.gamesUP.mapper.PublisherMapper;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import com.gamesUP.gamesUP.service.PublisherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PublisherResponse> findAll() {
        return publisherRepository.findAll().stream().map(publisherMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PublisherResponse findById(Long id) {
        return publisherMapper.toResponse(getOrThrow(id));
    }

    @Override
    public PublisherResponse create(PublisherRequest request) {
        Publisher publisher = Publisher.builder().name(request.name()).build();
        return publisherMapper.toResponse(publisherRepository.save(publisher));
    }

    @Override
    public PublisherResponse update(Long id, PublisherRequest request) {
        Publisher publisher = getOrThrow(id);
        publisher.setName(request.name());
        return publisherMapper.toResponse(publisherRepository.save(publisher));
    }

    @Override
    public void delete(Long id) {
        if (!publisherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Éditeur introuvable : " + id);
        }
        publisherRepository.deleteById(id);
    }

    private Publisher getOrThrow(Long id) {
        return publisherRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Éditeur introuvable : " + id));
    }
}
