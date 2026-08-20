package com.aws.carddemo.web.transaction;

import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.stereotype.Service;

@Service
public class TransactionAddService {
  private final TransactionService delegate;

  public TransactionAddService(TransactionService delegate) {
    this.delegate = delegate;
  }

  public Response<Transaction> add(TransactionService.TransactionAddRequest request) {
    return delegate.add(request);
  }
}
