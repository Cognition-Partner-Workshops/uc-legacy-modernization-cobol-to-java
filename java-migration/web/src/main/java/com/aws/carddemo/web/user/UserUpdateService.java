package com.aws.carddemo.web.user;

import com.aws.carddemo.domain.Usrsec;
import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.stereotype.Service;

@Service
public class UserUpdateService {
  private final UserAdminService delegate;

  public UserUpdateService(UserAdminService delegate) {
    this.delegate = delegate;
  }

  public Response<Usrsec> update(UserAdminService.UserUpdateRequest request) {
    return delegate.update(request);
  }
}
