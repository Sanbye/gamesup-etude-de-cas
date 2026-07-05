package com.gamesUP.gamesUP.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class UserControllerIT extends AbstractIntegrationTest {

    @Test
    void findAll_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void findAll_asClient_returnsForbidden() throws Exception {
        String clientToken = registerAndLoginClient("client.users@gamesup.test", "password123");

        mockMvc.perform(get("/api/users").header("Authorization", bearer(clientToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findAll_asAdmin_returnsOk() throws Exception {
        String adminToken = registerAndLoginAdmin("admin.users@gamesup.test", "password123");

        mockMvc.perform(get("/api/users").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void findById_asOwner_returnsOk() throws Exception {
        String token = registerAndLoginClient("owner@gamesup.test", "password123");
        User user = userRepository.findByEmail("owner@gamesup.test").orElseThrow();

        mockMvc.perform(get("/api/users/{id}", user.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void findById_asAnotherClient_returnsForbidden() throws Exception {
        registerAndLoginClient("victim@gamesup.test", "password123");
        User victim = userRepository.findByEmail("victim@gamesup.test").orElseThrow();
        String otherToken = registerAndLoginClient("intruder@gamesup.test", "password123");

        mockMvc.perform(get("/api/users/{id}", victim.getId()).header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden());
    }
}
