package com.aws.carddemo.web.transaction;

import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.stereotype.Service;

@Service
public class TransactionListService {
  private final TransactionService delegate;

  public TransactionListService(TransactionService delegate) {
    this.delegate = delegate;
  }

  public Response<TransactionService.TransactionPage> list(
      TransactionService.TransactionListRequest request) {
    return delegate.list(request);
  }
}
