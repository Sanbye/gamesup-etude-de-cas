package com.gamesUP.gamesUP.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gamesUP.gamesUP.dto.category.CategoryRequest;
import com.gamesUP.gamesUP.dto.game.GameRequest;
import com.gamesUP.gamesUP.dto.publisher.PublisherRequest;
import com.gamesUP.gamesUP.dto.purchase.PurchaseCreateRequest;
import com.gamesUP.gamesUP.dto.purchase.PurchaseLineRequest;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PurchaseControllerIT extends AbstractIntegrationTest {

    private long createGame(String adminToken, int stock) throws Exception {
        String categoryBody = mockMvc.perform(post("/api/categories")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Stratégie"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long categoryId = objectMapper.readTree(categoryBody).get("id").asLong();

        String publisherBody = mockMvc.perform(post("/api/publishers")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublisherRequest("Days of Wonder"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long publisherId = objectMapper.readTree(publisherBody).get("id").asLong();

        GameRequest gameRequest = new GameRequest(
                "7 Wonders", "desc", new BigDecimal("39.90"), 2010, stock, categoryId, publisherId, Set.of());
        String gameBody = mockMvc.perform(post("/api/games")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(gameBody).get("id").asLong();
    }

    @Test
    void create_decrementsStockAndComputesTotal() throws Exception {
        String adminToken = registerAndLoginAdmin("admin.purchase@gamesup.test", "password123");
        long gameId = createGame(adminToken, 10);

        String clientToken = registerAndLoginClient("client.purchase@gamesup.test", "password123");
        User client = userRepository.findByEmail("client.purchase@gamesup.test").orElseThrow();

        PurchaseCreateRequest request = new PurchaseCreateRequest(List.of(new PurchaseLineRequest(gameId, 2)));

        mockMvc.perform(post("/api/users/{userId}/purchases", client.getId())
                        .header("Authorization", bearer(clientToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(79.80));

        mockMvc.perform(get("/api/games/{id}", gameId)).andExpect(jsonPath("$.stockQuantity").value(8));
    }

    @Test
    void create_whenStockInsufficient_returnsConflict() throws Exception {
        String adminToken = registerAndLoginAdmin("admin.stock@gamesup.test", "password123");
        long gameId = createGame(adminToken, 1);

        String clientToken = registerAndLoginClient("client.stock@gamesup.test", "password123");
        User client = userRepository.findByEmail("client.stock@gamesup.test").orElseThrow();

        PurchaseCreateRequest request = new PurchaseCreateRequest(List.of(new PurchaseLineRequest(gameId, 5)));

        mockMvc.perform(post("/api/users/{userId}/purchases", client.getId())
                        .header("Authorization", bearer(clientToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void findByUser_asAnotherClient_returnsForbidden() throws Exception {
        registerAndLoginClient("owner.purchase@gamesup.test", "password123");
        User owner = userRepository.findByEmail("owner.purchase@gamesup.test").orElseThrow();
        String intruderToken = registerAndLoginClient("intruder.purchase@gamesup.test", "password123");

        mockMvc.perform(get("/api/users/{userId}/purchases", owner.getId())
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden());
    }
}
