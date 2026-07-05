package com.gamesUP.gamesUP.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gamesUP.gamesUP.dto.category.CategoryRequest;
import com.gamesUP.gamesUP.dto.game.GameRequest;
import com.gamesUP.gamesUP.dto.publisher.PublisherRequest;
import com.gamesUP.gamesUP.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class GameControllerIT extends AbstractIntegrationTest {

    private long createCategory(String adminToken, String name) throws Exception {
        String body = mockMvc.perform(post("/api/categories")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest(name))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long createPublisher(String adminToken, String name) throws Exception {
        String body = mockMvc.perform(post("/api/publishers")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublisherRequest(name))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void search_isPubliclyAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/games")).andExpect(status().isOk());
    }

    @Test
    void create_withoutAuthentication_returnsUnauthorized() throws Exception {
        GameRequest request =
                new GameRequest("7 Wonders", "desc", new BigDecimal("39.90"), 2010, 10, 1L, 1L, Set.of());

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_asClient_returnsForbidden() throws Exception {
        String clientToken = registerAndLoginClient("client.game@gamesup.test", "password123");
        GameRequest request =
                new GameRequest("7 Wonders", "desc", new BigDecimal("39.90"), 2010, 10, 1L, 1L, Set.of());

        mockMvc.perform(post("/api/games")
                        .header("Authorization", bearer(clientToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateSearchUpdateAndDeleteGame() throws Exception {
        String adminToken = registerAndLoginAdmin("admin.game@gamesup.test", "password123");
        long categoryId = createCategory(adminToken, "Stratégie");
        long publisherId = createPublisher(adminToken, "Days of Wonder");

        GameRequest createRequest = new GameRequest(
                "7 Wonders", "Jeu de civilisation", new BigDecimal("39.90"), 2010, 10, categoryId, publisherId, Set.of());

        String createBody = mockMvc.perform(post("/api/games")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("7 Wonders"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long gameId = objectMapper.readTree(createBody).get("id").asLong();

        mockMvc.perform(get("/api/games").param("name", "Wonders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("7 Wonders"));

        GameRequest updateRequest = new GameRequest(
                "7 Wonders Duel", "Version 2 joueurs", new BigDecimal("29.90"), 2015, 5, categoryId, publisherId, Set.of());
        mockMvc.perform(put("/api/games/{id}", gameId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("7 Wonders Duel"));

        mockMvc.perform(delete("/api/games/{id}", gameId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/games/{id}", gameId)).andExpect(status().isNotFound());
    }
}
