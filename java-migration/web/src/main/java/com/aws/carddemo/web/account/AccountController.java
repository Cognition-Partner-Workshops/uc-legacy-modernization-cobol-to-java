package com.aws.carddemo.web.account;

import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
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
      @RequestParam(name = "userId", required = false) String userId) {
    return service.view(
        accountId, new Context(fromProgram, "COACTVWC", "", "COAC", userId, "U", accountId, null));
  }

  @PutMapping("/{accountId}")
  public Response<AccountService.AccountView> update(
      @PathVariable("accountId") Long accountId,
      @RequestBody AccountService.AccountUpdateRequest request) {
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
            request.context()));
  }
}
