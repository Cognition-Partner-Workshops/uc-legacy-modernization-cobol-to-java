package com.aws.carddemo.web.menu;

import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MenuService {
  private static final List<MenuOption> USER_OPTIONS =
      List.of(
          new MenuOption(1, "Account View", "COACTVWC", "U"),
          new MenuOption(2, "Account Update", "COACTUPC", "U"),
          new MenuOption(3, "Credit Card List", "COCRDLIC", "U"),
          new MenuOption(4, "Credit Card View", "COCRDSLC", "U"),
          new MenuOption(5, "Credit Card Update", "COCRDUPC", "U"),
          new MenuOption(6, "Transaction List", "COTRN00C", "U"),
          new MenuOption(7, "Transaction View", "COTRN01C", "U"),
          new MenuOption(8, "Transaction Add", "COTRN02C", "U"),
          new MenuOption(9, "Transaction Reports", "CORPT00C", "U"),
          new MenuOption(10, "Bill Payment", "COBIL00C", "U"),
          new MenuOption(11, "Pending Authorization View", "COPAUS0C", "U"));
  private static final List<MenuOption> ADMIN_OPTIONS =
      List.of(
          new MenuOption(1, "User List (Security)", "COUSR00C", "A"),
          new MenuOption(2, "User Add (Security)", "COUSR01C", "A"),
          new MenuOption(3, "User Update (Security)", "COUSR02C", "A"),
          new MenuOption(4, "User Delete (Security)", "COUSR03C", "A"),
          new MenuOption(5, "Transaction Type List/Update (Db2)", "COTRTLIC", "A"),
          new MenuOption(6, "Transaction Type Maintenance (Db2)", "COTRTUPC", "A"));

  public Response<MenuResponse> menu(MenuRequest request, String userType) {
    boolean admin = "A".equalsIgnoreCase(userType);
    List<MenuOption> options = admin ? ADMIN_OPTIONS : USER_OPTIONS;
    if (request.option() == null) {
      return Response.ok(
          new MenuResponse(options), admin ? "COADM01C" : "COMEN01C", request.context());
    }
    if (request.option() < 1 || request.option() > options.size()) {
      return Response.error("Please enter a valid option number...", "option", request.context());
    }
    MenuOption option = options.get(request.option() - 1);
    if (!admin && !"U".equals(option.userType())) {
      return Response.error("No access to this option ...", "option", request.context());
    }
    Context old = request.context();
    Context next =
        new Context(
            old == null ? (admin ? "COADM01C" : "COMEN01C") : old.toProgram(),
            option.program(),
            old == null ? "" : old.fromTransaction(),
            option.program().substring(0, Math.min(4, option.program().length())),
            old == null ? null : old.userId(),
            userType,
            old == null ? null : old.accountId(),
            old == null ? null : old.cardNumber());
    return Response.ok(new MenuResponse(options), option.program(), next);
  }

  public record MenuRequest(Integer option, Context context) {}

  public record MenuResponse(List<MenuOption> options) {}

  public record MenuOption(int number, String name, String program, String userType) {}
}
