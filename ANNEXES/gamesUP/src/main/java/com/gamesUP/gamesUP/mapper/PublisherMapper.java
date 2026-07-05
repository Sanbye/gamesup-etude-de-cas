package com.gamesUP.gamesUP.mapper;

import com.gamesUP.gamesUP.dto.publisher.PublisherResponse;
import com.gamesUP.gamesUP.model.Publisher;
import org.springframework.stereotype.Component;

@Component
public class PublisherMapper {

    public PublisherResponse toResponse(Publisher publisher) {
        return new PublisherResponse(publisher.getId(), publisher.getName());
    }
}
