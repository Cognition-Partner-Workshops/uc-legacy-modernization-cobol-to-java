package com.carddemo.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.web.signon.SignonFailureException;
import com.carddemo.web.signon.SignonRequest;
import com.carddemo.web.signon.SignonResponse;
import com.carddemo.web.signon.SignonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SignonController.class)
@Import({com.carddemo.web.security.SecurityConfig.class, com.carddemo.web.security.LegacyPasswordEncoder.class})
class SignonControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private SignonService signonService;

    @Test
    void successfulAdminSignonReturnsTargetProgram() throws Exception {
        when(signonService.signon("ADMIN001", "PASSWORD"))
                .thenReturn(new SignonResponse("ADMIN001", "MARGARET", "GOLD", "A", "ROLE_ADMIN", "COADM01C"));

        mockMvc.perform(post("/api/signon")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SignonRequest("ADMIN001", "PASSWORD"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.nextProgram").value("COADM01C"));
    }

    @Test
    void blankIdReturnsBadRequestAndMessage() throws Exception {
        when(signonService.signon(any(), any()))
                .thenThrow(new SignonFailureException(HttpStatus.BAD_REQUEST, "Please enter User ID ..."));

        mockMvc.perform(post("/api/signon")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SignonRequest("", "PASSWORD"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please enter User ID ..."));
    }

    @Test
    void wrongPasswordReturnsUnauthorizedAndMessage() throws Exception {
        when(signonService.signon(any(), any()))
                .thenThrow(new SignonFailureException(HttpStatus.UNAUTHORIZED, "Wrong Password. Try again ..."));

        mockMvc.perform(post("/api/signon")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SignonRequest("ADMIN001", "NOPE"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Wrong Password. Try again ..."));
    }
}
