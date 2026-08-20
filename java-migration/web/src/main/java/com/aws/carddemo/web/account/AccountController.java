package com.aws.carddemo.web.account;

import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
  private final AccountService service;

  public AccountController(AccountService service) {
    this.service = service;
  }

  @GetMapping("/{accountId}")
  public Response<AccountService.AccountView> view(
      @PathVariable("accountId") Long accountId,
      @RequestParam(name = "fromProgram", required = false) String fromProgram,
      Authentication authentication) {
    String userId = authentication == null ? "" : authentication.getName();
    String userType =
        authentication != null
                && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            ? "A"
            : "U";
    return service.view(
        accountId,
        new Context(fromProgram, "COACTVWC", "", "COAC", userId, userType, accountId, null));
  }

  @PutMapping("/{accountId}")
  public Response<AccountService.AccountView> update(
      @PathVariable("accountId") Long accountId,
      @RequestBody AccountService.AccountUpdateRequest request,
      Authentication authentication) {
    String userId = authentication == null ? "" : authentication.getName();
    String userType =
        authentication != null
                && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            ? "A"
            : "U";
    Context context =
        new Context(
            request.context() == null ? "" : request.context().fromProgram(),
            "COACTUPC",
            "",
            "CAUP",
            userId,
            userType,
            accountId,
            null);
    return service.update(
        new AccountService.AccountUpdateRequest(
            accountId,
            request.activeStatus(),
            request.creditLimit(),
            request.cashCreditLimit(),
            request.openDate(),
            request.expiraionDate(),
            request.reissueDate(),
            request.groupId(),
            request.firstName(),
            request.middleName(),
            request.lastName(),
            request.addressLine1(),
            request.addressLine2(),
            request.city(),
            request.state(),
            request.country(),
            request.zip(),
            request.phone1(),
            request.phone2(),
            request.eftAccountId(),
            request.primaryCardHolder(),
            request.dob(),
            request.ficoScore(),
            request.preImage(),
            request.confirm(),
            context));
  }
}
