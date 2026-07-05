package com.gamesUP.gamesUP.dto.author;

import jakarta.validation.constraints.NotBlank;

public record AuthorRequest(@NotBlank String name) {}
