package com.gamesUP.gamesUP.mapper;

import com.gamesUP.gamesUP.dto.user.UserResponse;
import com.gamesUP.gamesUP.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole(),
                user.getCreatedAt());
    }
}
