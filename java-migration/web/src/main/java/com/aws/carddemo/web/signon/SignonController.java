package com.aws.carddemo.web.signon;

import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signon")
public class SignonController {
  private final SignonService service;

  public SignonController(SignonService service) {
    this.service = service;
  }

  @PostMapping
  public Response<SignonService.SignonResponse> signon(
      @RequestBody SignonService.SignonRequest request) {
    return service.signon(request);
  }
}
