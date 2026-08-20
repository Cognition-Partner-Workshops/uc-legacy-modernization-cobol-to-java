package com.aws.carddemo.web.signon;

import com.aws.carddemo.domain.Usrsec;
import com.aws.carddemo.domain.UsrsecRepository;
import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import com.aws.carddemo.web.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignonService {
  private final UsrsecRepository users;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokens;

  public SignonService(
      UsrsecRepository users, PasswordEncoder passwordEncoder, TokenService tokens) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.tokens = tokens;
  }

  public Response<SignonResponse> signon(SignonRequest request) {
    if (request.userId() == null || request.userId().isBlank()) {
      return Response.error("Please enter User ID ...", "userId", null);
    }
    if (request.password() == null || request.password().isBlank()) {
      return Response.error("Please enter Password ...", "password", null);
    }
    String userId = request.userId().trim().toUpperCase();
    Usrsec user;
    try {
      user = users.findBySecUsrId(userId).orElse(null);
    } catch (RuntimeException exception) {
      return Response.error("Unable to verify the User ...", "userId", null);
    }
    if (user == null) return Response.error("User not found. Try again ...", "userId", null);
    if (!passwordEncoder.matches(request.password().toUpperCase(), user.getSecUsrPwd())) {
      return Response.error("Wrong Password. Try again ...", "password", null);
    }
    String role = "A".equalsIgnoreCase(user.getSecUsrType()) ? "ROLE_ADMIN" : "ROLE_USER";
    Context context =
        new Context(
            "COSGN00C",
            role.equals("ROLE_ADMIN") ? "COADM01C" : "COMEN01C",
            "CCDA",
            role.equals("ROLE_ADMIN") ? "COAD" : "COMN",
            userId,
            user.getSecUsrType(),
            null,
            null);
    return Response.ok(
        new SignonResponse(tokens.issue(userId, role), userId, role), context.toProgram(), context);
  }

  public record SignonRequest(String userId, String password) {}

  public record SignonResponse(String token, String userId, String role) {}
}
