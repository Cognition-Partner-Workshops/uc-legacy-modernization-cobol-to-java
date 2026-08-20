package com.aws.carddemo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aws.carddemo.domain.UsrsecRepository;
import com.aws.carddemo.web.menu.MenuController;
import com.aws.carddemo.web.menu.MenuService;
import com.aws.carddemo.web.security.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MenuController.class)
@Import({com.aws.carddemo.web.security.SecurityConfig.class})
class SecurityRouteTest {
  @Autowired MockMvc mvc;

  @MockBean MenuService menu;
  @MockBean UsrsecRepository users;
  @MockBean TokenService tokenService;
  @MockBean com.aws.carddemo.web.user.UserAdminService userAdmin;

  @Test
  void unauthenticatedUserCannotOpenMenu() throws Exception {
    mvc.perform(post("/api/menu")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void userCannotOpenAdminMenu() throws Exception {
    mvc.perform(post("/api/admin/menu")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanOpenAdminMenu() throws Exception {
    mvc.perform(post("/api/admin/menu")).andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/admin/users", "/api/admin/users/USER001"})
  @WithMockUser(roles = "USER")
  void userCannotOpenAnyUserAdministrationRoute(String route) throws Exception {
    mvc.perform(post(route)).andExpect(status().isForbidden());
  }
}
