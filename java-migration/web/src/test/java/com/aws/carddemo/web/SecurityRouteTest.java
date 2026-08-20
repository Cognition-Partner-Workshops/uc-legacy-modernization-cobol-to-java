package com.aws.carddemo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aws.carddemo.domain.UsrsecRepository;
import com.aws.carddemo.web.menu.MenuController;
import com.aws.carddemo.web.menu.MenuService;
import com.aws.carddemo.web.security.TokenService;
import org.junit.jupiter.api.Test;
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
}
