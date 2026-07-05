package com.gamesUP.gamesUP.dto.user;

import com.gamesUP.gamesUP.model.Role;
import java.time.LocalDateTime;

public record UserResponse(
        Long id, String firstName, String lastName, String email, Role role, LocalDateTime createdAt) {}
