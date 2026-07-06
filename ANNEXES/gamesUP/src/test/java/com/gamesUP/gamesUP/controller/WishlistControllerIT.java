package com.gamesUP.gamesUP.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gamesUP.gamesUP.dto.category.CategoryRequest;
import com.gamesUP.gamesUP.dto.game.GameRequest;
import com.gamesUP.gamesUP.dto.publisher.PublisherRequest;
import com.gamesUP.gamesUP.dto.wishlist.WishlistAddItemRequest;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class WishlistControllerIT extends AbstractIntegrationTest {

    private long createGame(String adminToken) throws Exception {
        String categoryBody = mockMvc.perform(post("/api/categories")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Stratégie"))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long categoryId = objectMapper.readTree(categoryBody).get("id").asLong();

        String publisherBody = mockMvc.perform(post("/api/publishers")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublisherRequest("Days of Wonder"))))
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
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(gameBody).get("id").asLong();
    }

    @Test
    void addAndRemoveItem_asOwner_works() throws Exception {
        String adminToken = registerAndLoginAdmin("admin.wishlist@gamesup.test", "password123");
        long gameId = createGame(adminToken);

        String ownerToken = registerAndLoginClient("owner.wishlist@gamesup.test", "password123");
        User owner = userRepository.findByEmail("owner.wishlist@gamesup.test").orElseThrow();

        mockMvc.perform(post("/api/users/{userId}/wishlist/items", owner.getId())
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WishlistAddItemRequest(gameId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].gameId").value(gameId));

        mockMvc.perform(get("/api/users/{userId}/wishlist", owner.getId()).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void accessWishlist_asAnotherClient_returnsForbidden() throws Exception {
        registerAndLoginClient("owner.wishlist2@gamesup.test", "password123");
        User owner = userRepository.findByEmail("owner.wishlist2@gamesup.test").orElseThrow();
        String intruderToken = registerAndLoginClient("intruder.wishlist@gamesup.test", "password123");

        mockMvc.perform(get("/api/users/{userId}/wishlist", owner.getId())
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden());
    }
}
