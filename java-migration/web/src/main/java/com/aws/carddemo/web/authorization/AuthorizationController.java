package com.aws.carddemo.web.authorization;

import com.aws.carddemo.domain.PendingAuthDetail;
import com.aws.carddemo.web.common.WebTypes.Response;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authorizations")
public class AuthorizationController {
  private final AuthorizationService service;

  public AuthorizationController(AuthorizationService service) {
    this.service = service;
  }

  @GetMapping("/summary/{accountId}")
  public Response<List<PendingAuthDetail>> summary(
      @PathVariable Long accountId, @RequestParam(required = false) Integer page) {
    List<PendingAuthDetail> result = service.details(accountId);
    int from = Math.max(0, (page == null ? 0 : page) * 5);
    if (from >= result.size()) {
      return Response.error("Already at the last Authorization...", "accountId", null);
    }
    return Response.ok(result.subList(from, Math.min(from + 5, result.size())), "COPAUS0C", null);
  }

  @GetMapping("/detail/{id}")
  public Response<PendingAuthDetail> detail(@PathVariable Long id) {
    return service
        .detail(id)
        .map(value -> Response.ok(value, "COPAUS1C", null))
        .orElseGet(() -> Response.error("Authorization not found", "authorization", null));
  }

  @PostMapping("/detail/{id}/fraud")
  public Response<String> fraud(@PathVariable Long id, @RequestParam boolean marked) {
    return Response.ok(service.setFraud(id, marked), "COPAUS1C", null);
  }
}
