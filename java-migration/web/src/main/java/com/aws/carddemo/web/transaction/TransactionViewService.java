package com.aws.carddemo.web.transaction;

import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.stereotype.Service;

@Service
public class TransactionViewService {
  private final TransactionService delegate;

  public TransactionViewService(TransactionService delegate) {
    this.delegate = delegate;
  }

  public Response<Transaction> view(String transactionId, Context context) {
    return delegate.view(transactionId, context);
  }
}
