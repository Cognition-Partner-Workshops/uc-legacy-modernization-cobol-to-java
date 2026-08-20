package com.aws.carddemo.web.user;

import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.stereotype.Service;

@Service
public class UserListService {
  private final UserAdminService delegate;

  public UserListService(UserAdminService delegate) {
    this.delegate = delegate;
  }

  public Response<UserAdminService.UserPage> list(UserAdminService.UserListRequest request) {
    return delegate.list(request);
  }
}
