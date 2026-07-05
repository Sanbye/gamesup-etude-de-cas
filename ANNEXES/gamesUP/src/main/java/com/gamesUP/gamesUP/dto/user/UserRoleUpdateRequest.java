package com.gamesUP.gamesUP.dto.user;

import com.gamesUP.gamesUP.model.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(@NotNull Role role) {}
