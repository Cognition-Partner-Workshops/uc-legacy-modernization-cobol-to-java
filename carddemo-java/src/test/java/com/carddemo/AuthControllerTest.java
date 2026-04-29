package com.carddemo;

import com.carddemo.dto.SignInRequest;
import com.carddemo.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void successfulLogin_returnsJwtAndUserInfo() throws Exception {
        SignInRequest request = new SignInRequest("ADMIN001", "PASSWORD");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.userId", is("ADMIN001")))
                .andExpect(jsonPath("$.firstName", is("ADMIN")))
                .andExpect(jsonPath("$.lastName", is("USER")))
                .andExpect(jsonPath("$.userType", is("A")));
    }

    @Test
    void successfulLogin_regularUser() throws Exception {
        SignInRequest request = new SignInRequest("USER0001", "PASSWORD");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.userId", is("USER0001")))
                .andExpect(jsonPath("$.userType", is("U")));
    }

    @Test
    void successfulLogin_caseInsensitiveUserId() throws Exception {
        SignInRequest request = new SignInRequest("admin001", "PASSWORD");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("ADMIN001")));
    }

    @Test
    void wrongPassword_returns401() throws Exception {
        SignInRequest request = new SignInRequest("ADMIN001", "WRONGPASS");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message",
                        is("Sign in Unsuccessful. Userid / Password does not match.")));
    }

    @Test
    void nonExistentUser_returns401_withSameMessage() throws Exception {
        SignInRequest request = new SignInRequest("UNKNOWN1", "PASSWORD");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message",
                        is("Sign in Unsuccessful. Userid / Password does not match.")));
    }

    @Test
    void tokenValidation_worksCorrectly() throws Exception {
        SignInRequest request = new SignInRequest("ADMIN001", "PASSWORD");

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // Validate the token
        assert jwtUtil.validateToken(token);
        assert "ADMIN001".equals(jwtUtil.getUserIdFromToken(token));

        // Verify claims
        var claims = jwtUtil.parseToken(token);
        assert "A".equals(claims.get("userType", String.class));
        assert "ADMIN".equals(claims.get("firstName", String.class));
        assert "USER".equals(claims.get("lastName", String.class));
    }

    @Test
    void invalidToken_isRejected() {
        assert !jwtUtil.validateToken("invalid.token.value");
        assert !jwtUtil.validateToken("");
        assert !jwtUtil.validateToken(null);
    }
}
