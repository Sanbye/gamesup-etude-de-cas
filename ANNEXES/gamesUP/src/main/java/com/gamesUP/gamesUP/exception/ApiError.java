package com.gamesUP.gamesUP.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(LocalDateTime timestamp, int status, String error, List<String> messages) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(LocalDateTime.now(), status, error, List.of(message));
    }

    public static ApiError of(int status, String error, List<String> messages) {
        return new ApiError(LocalDateTime.now(), status, error, messages);
    }
}
