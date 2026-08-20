package com.aws.carddemo.web.user;

import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.stereotype.Service;

@Service
public class UserDeleteService {
  private final UserAdminService delegate;

  public UserDeleteService(UserAdminService delegate) {
    this.delegate = delegate;
  }

  public Response<Void> delete(UserAdminService.UserDeleteRequest request) {
    return delegate.delete(request);
  }
}
