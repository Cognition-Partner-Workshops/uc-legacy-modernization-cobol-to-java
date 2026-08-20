package com.aws.carddemo.web.menu;

import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MenuController {
  private final MenuService service;

  public MenuController(MenuService service) {
    this.service = service;
  }

  @PostMapping("/menu")
  public Response<MenuService.MenuResponse> menu(
      @RequestBody(required = false) MenuService.MenuRequest request,
      Authentication authentication) {
    String userType =
        authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            ? "A"
            : "U";
    return service.menu(
        request == null ? new MenuService.MenuRequest(null, null) : request, userType);
  }

  @PostMapping("/admin/menu")
  public Response<MenuService.MenuResponse> adminMenu(
      @RequestBody(required = false) MenuService.MenuRequest request,
      Authentication authentication) {
    return service.menu(request == null ? new MenuService.MenuRequest(null, null) : request, "A");
  }
}
