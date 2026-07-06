package com.gamesUP.gamesUP.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gamesUP.gamesUP.dto.category.CategoryRequest;
import com.gamesUP.gamesUP.dto.game.GameRequest;
import com.gamesUP.gamesUP.dto.publisher.PublisherRequest;
import com.gamesUP.gamesUP.dto.review.ReviewRequest;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ReviewControllerIT extends AbstractIntegrationTest {

    private long createGame(String adminToken) throws Exception {
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
                "7 Wonders", "desc", new BigDecimal("39.90"), 2010, 10, Set.of(categoryId), publisherId, Set.of());
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
    void findByGame_isPubliclyAccessible() throws Exception {
        String adminToken = registerAndLoginAdmin("admin.review@gamesup.test", "password123");
        long gameId = createGame(adminToken);

        mockMvc.perform(get("/api/games/{gameId}/reviews", gameId)).andExpect(status().isOk());
    }

    @Test
    void ownerCanUpdateAndDeleteTheirReview_othersCannot() throws Exception {
        String adminToken = registerAndLoginAdmin("admin.review2@gamesup.test", "password123");
        long gameId = createGame(adminToken);

        String ownerToken = registerAndLoginClient("owner.review@gamesup.test", "password123");
        User owner = userRepository.findByEmail("owner.review@gamesup.test").orElseThrow();

        String reviewBody = mockMvc.perform(post("/api/users/{userId}/reviews", owner.getId())
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReviewRequest(gameId, "Top jeu", 5))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long reviewId = objectMapper.readTree(reviewBody).get("id").asLong();

        String otherToken = registerAndLoginClient("other.review@gamesup.test", "password123");
        mockMvc.perform(delete("/api/reviews/{id}", reviewId).header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/reviews/{id}", reviewId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNoContent());
    }
}
