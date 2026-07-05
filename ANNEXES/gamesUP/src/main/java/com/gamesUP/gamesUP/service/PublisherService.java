package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.publisher.PublisherRequest;
import com.gamesUP.gamesUP.dto.publisher.PublisherResponse;
import java.util.List;

public interface PublisherService {

    List<PublisherResponse> findAll();

    PublisherResponse findById(Long id);

    PublisherResponse create(PublisherRequest request);

    PublisherResponse update(Long id, PublisherRequest request);

    void delete(Long id);
}
