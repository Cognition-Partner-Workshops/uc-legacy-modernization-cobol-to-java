package com.aws.carddemo.web.user;

import com.aws.carddemo.domain.Usrsec;
import com.aws.carddemo.domain.UsrsecRepository;
import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminService {
  private final UsrsecRepository users;

  public UserAdminService(UsrsecRepository users) {
    this.users = users;
  }

  public Response<UserPage> list(UserListRequest request) {
    List<Usrsec> rows = users.findAll();
    rows.sort(java.util.Comparator.comparing(Usrsec::getSecUsrId));
    int page = Math.max(0, request.page());
    int from = Math.min(page * 10, rows.size());
    int to = Math.min(from + 10, rows.size());
    if (from == to && page > 0) {
      return Response.error(
          "You have reached the bottom of the page...", "page", request.context());
    }
    return Response.ok(
        new UserPage(rows.subList(from, to), page, 10, to < rows.size(), page > 0),
        "COUSR00C",
        request.context());
  }

  @Transactional
  public Response<Usrsec> add(UserAddRequest request) {
    String validation =
        validate(
            request.firstName(),
            request.lastName(),
            request.userId(),
            request.password(),
            request.userType());
    if (!validation.isEmpty())
      return Response.error(validation, field(validation), request.context());
    String id = request.userId().trim().toUpperCase();
    if (users.existsById(id))
      return Response.error("User ID already exist...", "userId", request.context());
    Usrsec user = new Usrsec();
    user.setSecUsrId(id);
    user.setSecUsrFname(request.firstName().trim());
    user.setSecUsrLname(request.lastName().trim());
    user.setSecUsrPwd(request.password());
    user.setSecUsrType(request.userType().trim().toUpperCase());
    return Response.ok(users.save(user), "COUSR01C", request.context());
  }

  @Transactional
  public Response<Usrsec> update(UserUpdateRequest request) {
    String validation =
        validate(
            request.firstName(),
            request.lastName(),
            request.userId(),
            request.password(),
            request.userType());
    if (!validation.isEmpty())
      return Response.error(validation, field(validation), request.context());
    Usrsec user = users.findById(request.userId().trim().toUpperCase()).orElse(null);
    if (user == null) return Response.error("User ID NOT found...", "userId", request.context());
    if (!request.confirm())
      return Response.error("Press PF5 key to save your updates ...", "confirm", request.context());
    user.setSecUsrFname(request.firstName().trim());
    user.setSecUsrLname(request.lastName().trim());
    user.setSecUsrPwd(request.password());
    user.setSecUsrType(request.userType().trim().toUpperCase());
    return Response.ok(users.save(user), "COUSR02C", request.context());
  }

  @Transactional
  public Response<Void> delete(UserDeleteRequest request) {
    if (request.userId() == null || request.userId().isBlank())
      return Response.error("User ID can NOT be empty...", "userId", request.context());
    String id = request.userId().trim().toUpperCase();
    if (!users.existsById(id))
      return Response.error("User ID NOT found...", "userId", request.context());
    if (!request.confirm())
      return Response.error("Press PF5 key to delete this user ...", "confirm", request.context());
    users.deleteById(id);
    return Response.ok(null, "COUSR03C", request.context());
  }

  private static String validate(
      String first, String last, String id, String password, String type) {
    if (blank(first)) return "First Name can NOT be empty...";
    if (blank(last)) return "Last Name can NOT be empty...";
    if (blank(id)) return "User ID can NOT be empty...";
    if (blank(password)) return "Password can NOT be empty...";
    if (blank(type)) return "User Type can NOT be empty...";
    return "";
  }

  private static String field(String message) {
    if (message.startsWith("First")) return "firstName";
    if (message.startsWith("Last")) return "lastName";
    if (message.startsWith("User ID")) return "userId";
    if (message.startsWith("Password")) return "password";
    return "userType";
  }

  private static boolean blank(String value) {
    return value == null || value.isBlank();
  }

  public record UserListRequest(int page, Context context) {}

  public record UserPage(
      List<Usrsec> users, int page, int pageSize, boolean nextPage, boolean previousPage) {}

  public record UserAddRequest(
      String firstName,
      String lastName,
      String userId,
      String password,
      String userType,
      Context context) {}

  public record UserUpdateRequest(
      String userId,
      String firstName,
      String lastName,
      String password,
      String userType,
      boolean confirm,
      Context context) {}

  public record UserDeleteRequest(String userId, boolean confirm, Context context) {}
}
