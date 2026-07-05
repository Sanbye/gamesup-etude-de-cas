package com.gamesUP.gamesUP.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesUP.gamesUP.dto.auth.LoginRequest;
import com.gamesUP.gamesUP.dto.auth.LoginResponse;
import com.gamesUP.gamesUP.dto.user.UserRegisterRequest;
import com.gamesUP.gamesUP.model.Role;
import com.gamesUP.gamesUP.model.User;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    /** Enregistre un nouveau client via l'API et retourne son token JWT. */
    protected String registerAndLoginClient(String email, String password) throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest("Jean", "Dupont", email, password);
        mockMvc.perform(post("/api/users/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        return login(email, password);
    }

    /** Enregistre un client puis le promeut administrateur directement en base, et retourne son token. */
    protected String registerAndLoginAdmin(String email, String password) throws Exception {
        registerAndLoginClient(email, password);
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        return login(email, password);
    }

    protected String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(body, LoginResponse.class).token();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
