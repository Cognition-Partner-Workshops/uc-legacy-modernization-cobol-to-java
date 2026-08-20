package com.aws.carddemo.web.user;

import com.aws.carddemo.domain.Usrsec;
import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.stereotype.Service;

@Service
public class UserAddService {
  private final UserAdminService delegate;

  public UserAddService(UserAdminService delegate) {
    this.delegate = delegate;
  }

  public Response<Usrsec> add(UserAdminService.UserAddRequest request) {
    return delegate.add(request);
  }
}
