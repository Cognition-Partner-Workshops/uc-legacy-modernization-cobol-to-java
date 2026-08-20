package com.aws.carddemo.web.bill;

import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bill-payments")
public class BillPaymentController {
  private final BillPaymentService service;

  public BillPaymentController(BillPaymentService service) {
    this.service = service;
  }

  @PostMapping
  public Response<BillPaymentService.BillPaymentResult> pay(
      @RequestBody BillPaymentService.BillPaymentRequest request) {
    return service.pay(request);
  }
}
