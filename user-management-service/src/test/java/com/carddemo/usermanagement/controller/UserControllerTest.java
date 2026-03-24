package com.carddemo.usermanagement.controller;

import com.carddemo.usermanagement.dto.UserResponse;
import com.carddemo.usermanagement.exception.DuplicateUserException;
import com.carddemo.usermanagement.exception.UserNotFoundException;
import com.carddemo.usermanagement.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("GET /api/users — replaces COUSR00C")
    class ListUsersEndpoint {

        @Test
        @DisplayName("returns paginated user list")
        void returnsPaginatedList() throws Exception {
            UserResponse user1 = new UserResponse("USER0001", "John", "Smith", "Regular");
            UserResponse user2 = new UserResponse("USER0002", "Jane", "Doe", "Regular");
            Page<UserResponse> page = new PageImpl<>(List.of(user1, user2));
            when(userService.listUsers(anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(get("/api/users").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].userId").value("USER0001"))
                    .andExpect(jsonPath("$.content[1].userId").value("USER0002"));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}")
    class GetUserEndpoint {

        @Test
        @DisplayName("returns user when found")
        void returnsUser() throws Exception {
            UserResponse user = new UserResponse("USER0001", "John", "Smith", "Regular");
            when(userService.getUser("USER0001")).thenReturn(user);

            mockMvc.perform(get("/api/users/USER0001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value("USER0001"))
                    .andExpect(jsonPath("$.firstName").value("John"));
        }

        @Test
        @DisplayName("returns 404 when user not found — mirrors DFHRESP(NOTFND)")
        void returns404WhenNotFound() throws Exception {
            when(userService.getUser("UNKNOWN")).thenThrow(new UserNotFoundException("UNKNOWN"));

            mockMvc.perform(get("/api/users/UNKNOWN"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("User ID NOT found: UNKNOWN"));
        }
    }

    @Nested
    @DisplayName("POST /api/users — replaces COUSR01C")
    class CreateUserEndpoint {

        @Test
        @DisplayName("creates user and returns 201 with success message")
        void createsUserSuccessfully() throws Exception {
            UserResponse created = new UserResponse("NEWUSR01", "Alice", "Wonder", "Regular");
            when(userService.createUser(any())).thenReturn(created);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userId": "NEWUSR01",
                                      "firstName": "Alice",
                                      "lastName": "Wonder",
                                      "password": "PASS1234",
                                      "userType": "R"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("User NEWUSR01 has been added ..."))
                    .andExpect(jsonPath("$.user.userId").value("NEWUSR01"));
        }

        @Test
        @DisplayName("returns 409 on duplicate user ID — mirrors DFHRESP(DUPREC)")
        void returns409OnDuplicate() throws Exception {
            when(userService.createUser(any())).thenThrow(new DuplicateUserException("USER0001"));

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userId": "USER0001",
                                      "firstName": "John",
                                      "lastName": "Smith",
                                      "password": "PASS0001",
                                      "userType": "R"
                                    }
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("User ID already exists: USER0001"));
        }

        @Test
        @DisplayName("returns 400 when first name is empty — matches COUSR01C validation")
        void returns400WhenFirstNameEmpty() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userId": "NEWUSR01",
                                      "firstName": "",
                                      "lastName": "Wonder",
                                      "password": "PASS1234",
                                      "userType": "R"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.messages").isArray());
        }

        @Test
        @DisplayName("returns 400 when user type is empty — matches COUSR01C validation")
        void returns400WhenUserTypeEmpty() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "userId": "NEWUSR01",
                                      "firstName": "Alice",
                                      "lastName": "Wonder",
                                      "password": "PASS1234",
                                      "userType": ""
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when all fields are missing")
        void returns400WhenAllFieldsMissing() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.messages").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{userId} — replaces COUSR02C")
    class UpdateUserEndpoint {

        @Test
        @DisplayName("updates user and returns success message")
        void updatesUserSuccessfully() throws Exception {
            UserResponse updated = new UserResponse("USER0001", "Johnny", "Smith", "Regular");
            when(userService.updateUser(eq("USER0001"), any())).thenReturn(updated);

            mockMvc.perform(put("/api/users/USER0001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": "Johnny",
                                      "lastName": "Smith",
                                      "password": "NEWPASS1",
                                      "userType": "R"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User USER0001 has been updated ..."))
                    .andExpect(jsonPath("$.user.firstName").value("Johnny"));
        }

        @Test
        @DisplayName("returns 404 when user not found")
        void returns404WhenNotFound() throws Exception {
            when(userService.updateUser(eq("UNKNOWN"), any()))
                    .thenThrow(new UserNotFoundException("UNKNOWN"));

            mockMvc.perform(put("/api/users/UNKNOWN")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": "Test",
                                      "lastName": "User",
                                      "password": "TESTPASS",
                                      "userType": "R"
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 400 when password is empty — matches COUSR02C validation")
        void returns400WhenPasswordEmpty() throws Exception {
            mockMvc.perform(put("/api/users/USER0001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": "John",
                                      "lastName": "Smith",
                                      "password": "",
                                      "userType": "R"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{userId} — replaces COUSR03C")
    class DeleteUserEndpoint {

        @Test
        @DisplayName("deletes user and returns success message")
        void deletesUserSuccessfully() throws Exception {
            doNothing().when(userService).deleteUser("USER0001");

            mockMvc.perform(delete("/api/users/USER0001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User USER0001 has been deleted ..."));
        }

        @Test
        @DisplayName("returns 404 when user not found")
        void returns404WhenNotFound() throws Exception {
            doThrow(new UserNotFoundException("UNKNOWN")).when(userService).deleteUser("UNKNOWN");

            mockMvc.perform(delete("/api/users/UNKNOWN"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("User ID NOT found: UNKNOWN"));
        }
    }
}
