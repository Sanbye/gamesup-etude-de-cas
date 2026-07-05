package com.gamesUP.gamesUP.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gamesUP.gamesUP.client.RecommendationClient;
import com.gamesUP.gamesUP.dto.recommendation.GameRecommendationResponse;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.support.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

class RecommendationControllerIT extends AbstractIntegrationTest {

    @MockBean
    private RecommendationClient recommendationClient;

    @Test
    void recommendations_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/recommendations", 1L)).andExpect(status().isUnauthorized());
    }

    @Test
    void recommendations_asOwner_returnsRecommendationsFromPythonApi() throws Exception {
        String token = registerAndLoginClient("owner.reco@gamesup.test", "password123");
        User owner = userRepository.findByEmail("owner.reco@gamesup.test").orElseThrow();

        when(recommendationClient.fetchRecommendations(any()))
                .thenReturn(List.of(new GameRecommendationResponse(101L, "Pandemic", 0.8)));

        mockMvc.perform(get("/api/users/{userId}/recommendations", owner.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gameId").value(101))
                .andExpect(jsonPath("$[0].gameName").value("Pandemic"));
    }

    @Test
    void recommendations_asAnotherClient_returnsForbidden() throws Exception {
        registerAndLoginClient("owner.reco2@gamesup.test", "password123");
        User owner = userRepository.findByEmail("owner.reco2@gamesup.test").orElseThrow();
        String intruderToken = registerAndLoginClient("intruder.reco@gamesup.test", "password123");

        mockMvc.perform(get("/api/users/{userId}/recommendations", owner.getId())
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden());
    }
}
