package com.carddemo.controller;

import com.carddemo.config.SecurityConfig;
import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.service.UserSecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSecurityService userSecurityService;

    @MockBean
    private UserSecurityRepository userSecurityRepository;

    private UserSecurity createTestUser() {
        UserSecurity user = new UserSecurity();
        user.setUserId("TESTUSER");
        user.setPassword("TESTPASS");
        user.setUserType("U");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = {"ADMIN"})
    void listUsers_returnsUserList() throws Exception {
        when(userSecurityService.listUsers(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(createTestUser())));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-list"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void listUsers_regularUser_returns403() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = {"ADMIN"})
    void showAddForm_returnsForm() throws Exception {
        mockMvc.perform(get("/admin/users/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-add"))
                .andExpect(model().attributeExists("userForm"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = {"ADMIN"})
    void addUser_redirectsToUserList() throws Exception {
        when(userSecurityService.addUser(any(UserSecurity.class))).thenReturn(createTestUser());

        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("userId", "NEWUSER1")
                        .param("password", "NEWPASS1")
                        .param("userType", "U")
                        .param("firstName", "New")
                        .param("lastName", "User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = {"ADMIN"})
    void editUser_returnsUpdateForm() throws Exception {
        when(userSecurityService.getUser("TESTUSER")).thenReturn(createTestUser());

        mockMvc.perform(get("/admin/users/TESTUSER/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-update"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = {"ADMIN"})
    void updateUser_redirectsToUserList() throws Exception {
        when(userSecurityService.updateUser(eq("TESTUSER"), any(UserSecurity.class)))
                .thenReturn(createTestUser());

        mockMvc.perform(post("/admin/users/TESTUSER")
                        .with(csrf())
                        .param("password", "NEWPASS")
                        .param("userType", "A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = {"ADMIN"})
    void deleteUser_redirectsToUserList() throws Exception {
        doNothing().when(userSecurityService).deleteUser("TESTUSER");

        mockMvc.perform(post("/admin/users/TESTUSER/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = {"ADMIN"})
    void showDeleteConfirmation_returnsDeletePage() throws Exception {
        when(userSecurityService.getUser("TESTUSER")).thenReturn(createTestUser());

        mockMvc.perform(get("/admin/users/TESTUSER/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-delete"))
                .andExpect(model().attributeExists("user"));
    }
}
