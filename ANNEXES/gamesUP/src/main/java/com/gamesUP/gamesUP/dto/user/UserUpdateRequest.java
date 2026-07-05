package com.gamesUP.gamesUP.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(@NotBlank String firstName, @NotBlank String lastName, @NotBlank @Email String email) {}
