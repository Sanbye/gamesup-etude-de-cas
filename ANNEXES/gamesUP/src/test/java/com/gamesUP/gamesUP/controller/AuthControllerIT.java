package com.gamesUP.gamesUP.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gamesUP.gamesUP.dto.auth.LoginRequest;
import com.gamesUP.gamesUP.dto.user.UserRegisterRequest;
import com.gamesUP.gamesUP.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class AuthControllerIT extends AbstractIntegrationTest {

    @Test
    void register_thenLogin_returnsToken() throws Exception {
        UserRegisterRequest registerRequest =
                new UserRegisterRequest("Jean", "Dupont", "jean.auth@gamesup.test", "password123");
        mockMvc.perform(post("/api/users/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CLIENT"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("jean.auth@gamesup.test", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void register_withDuplicateEmail_returnsConflict() throws Exception {
        UserRegisterRequest registerRequest =
                new UserRegisterRequest("Jean", "Dupont", "duplicate@gamesup.test", "password123");
        mockMvc.perform(post("/api/users/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        registerAndLoginClient("wrongpass@gamesup.test", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("wrongpass@gamesup.test", "not-the-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_withInvalidPayload_returnsBadRequest() throws Exception {
        UserRegisterRequest invalidRequest = new UserRegisterRequest("", "Dupont", "not-an-email", "short");

        mockMvc.perform(post("/api/users/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
