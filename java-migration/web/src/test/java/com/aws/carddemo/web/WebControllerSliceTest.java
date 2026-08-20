package com.aws.carddemo.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aws.carddemo.web.common.WebTypes.Response;
import com.aws.carddemo.web.menu.MenuController;
import com.aws.carddemo.web.menu.MenuService;
import com.aws.carddemo.web.signon.SignonController;
import com.aws.carddemo.web.signon.SignonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({SignonController.class, MenuController.class})
@Import(TestWebSecurityConfiguration.class)
@ActiveProfiles("test")
class WebControllerSliceTest {
  @Autowired MockMvc mvc;

  @MockBean SignonService signon;
  @MockBean MenuService menu;

  @Test
  void signonErrorCarriesMessageAndField() throws Exception {
    when(signon.signon(any()))
        .thenReturn(Response.error("Please enter User ID ...", "userId", null));

    mvc.perform(
            post("/api/signon")
                .contentType("application/json")
                .content("{\"userId\":\"\",\"password\":\"x\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Please enter User ID ..."))
        .andExpect(jsonPath("$.errorField").value("userId"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void menuOptionsSerializeWithProgramAndUserType() throws Exception {
    when(menu.menu(any(), any()))
        .thenReturn(
            Response.ok(
                new MenuService.MenuResponse(
                    java.util.List.of(
                        new MenuService.MenuOption(1, "Account View", "COACTVWC", "U"))),
                "COACTVWC",
                null));

    mvc.perform(post("/api/menu").contentType("application/json").content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.options[0].program").value("COACTVWC"))
        .andExpect(jsonPath("$.data.options[0].userType").value("U"));
  }
}
