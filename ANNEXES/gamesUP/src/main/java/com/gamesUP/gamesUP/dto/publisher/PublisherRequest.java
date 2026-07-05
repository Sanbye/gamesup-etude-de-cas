package com.gamesUP.gamesUP.dto.publisher;

import jakarta.validation.constraints.NotBlank;

public record PublisherRequest(@NotBlank String name) {}
