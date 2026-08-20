package com.aws.carddemo.web.user;

import com.aws.carddemo.domain.Usrsec;
import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {
  private final UserListService listService;
  private final UserAddService addService;
  private final UserUpdateService updateService;
  private final UserDeleteService deleteService;

  public UserAdminController(
      UserListService listService,
      UserAddService addService,
      UserUpdateService updateService,
      UserDeleteService deleteService) {
    this.listService = listService;
    this.addService = addService;
    this.updateService = updateService;
    this.deleteService = deleteService;
  }

  @GetMapping
  public Response<UserAdminService.UserPage> list(
      @RequestParam(name = "page", defaultValue = "0") int page) {
    return listService.list(
        new UserAdminService.UserListRequest(
            page, new Context("", "COUSR00C", "", "CU00", null, "A", null, null)));
  }

  @PostMapping
  public Response<Usrsec> add(@RequestBody UserAdminService.UserAddRequest request) {
    return addService.add(request);
  }

  @PutMapping("/{userId}")
  public Response<Usrsec> update(
      @PathVariable("userId") String userId,
      @RequestBody UserAdminService.UserUpdateRequest request) {
    return updateService.update(
        new UserAdminService.UserUpdateRequest(
            userId,
            request.firstName(),
            request.lastName(),
            request.password(),
            request.userType(),
            request.confirm(),
            request.context()));
  }

  @DeleteMapping("/{userId}")
  public Response<Void> delete(
      @PathVariable("userId") String userId,
      @RequestBody UserAdminService.UserDeleteRequest request) {
    return deleteService.delete(
        new UserAdminService.UserDeleteRequest(userId, request.confirm(), request.context()));
  }
}
