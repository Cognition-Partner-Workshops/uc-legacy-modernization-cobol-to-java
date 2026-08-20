package com.aws.carddemo.web.transaction;

import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
  private final TransactionListService listService;
  private final TransactionViewService viewService;
  private final TransactionAddService addService;

  public TransactionController(
      TransactionListService listService,
      TransactionViewService viewService,
      TransactionAddService addService) {
    this.listService = listService;
    this.viewService = viewService;
    this.addService = addService;
  }

  @GetMapping
  public Response<TransactionService.TransactionPage> list(
      @RequestParam(name = "accountId", required = false) Long accountId,
      @RequestParam(name = "cardNumber", required = false) String cardNumber,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "direction", defaultValue = "FORWARD") String direction) {
    return listService.list(
        new TransactionService.TransactionListRequest(
            accountId,
            cardNumber,
            page,
            direction,
            new Context("", "COTRN00C", "", "CT00", null, "U", accountId, cardNumber)));
  }

  @GetMapping("/{transactionId}")
  public Response<Transaction> view(@PathVariable("transactionId") String transactionId) {
    return viewService.view(
        transactionId, new Context("", "COTRN01C", "", "CT01", null, "U", null, null));
  }

  @PostMapping
  public Response<Transaction> add(@RequestBody TransactionService.TransactionAddRequest request) {
    return addService.add(request);
  }
}
