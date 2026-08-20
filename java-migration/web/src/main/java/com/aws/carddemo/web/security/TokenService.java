package com.aws.carddemo.web.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
  private final String secret;

  public TokenService(
      @Value("${carddemo.security.jwt-secret:dev-only-change-this-carddemo-key}") String secret) {
    this.secret = secret;
  }

  public String issue(String userId, String role) {
    long expiry = Instant.now().plusSeconds(3600).getEpochSecond();
    String header =
        ENCODER.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    String payload =
        ENCODER.encodeToString(
            ("{\"sub\":\""
                    + escape(userId)
                    + "\",\"role\":\""
                    + role
                    + "\",\"exp\":"
                    + expiry
                    + "}")
                .getBytes(StandardCharsets.UTF_8));
    return header + "." + payload + "." + sign(header + "." + payload);
  }

  public Claims verify(String token) {
    try {
      String[] parts = token.split("\\.", -1);
      if (parts.length != 3 || !sign(parts[0] + "." + parts[1]).equals(parts[2])) return null;
      String payload = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
      String subject = value(payload, "sub");
      String role = value(payload, "role");
      long expiry = Long.parseLong(value(payload, "exp"));
      return expiry > Instant.now().getEpochSecond() ? new Claims(subject, role) : null;
    } catch (RuntimeException e) {
      return null;
    }
  }

  private String sign(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static String value(String json, String key) {
    String marker = "\"" + key + "\":";
    int start = json.indexOf(marker) + marker.length();
    if (start <= marker.length() - 1) throw new IllegalArgumentException("missing claim");
    if (json.charAt(start) == '"') {
      int end = json.indexOf('"', start + 1);
      return json.substring(start + 1, end);
    }
    int end = json.indexOf(',', start);
    return json.substring(start, end < 0 ? json.length() - 1 : end);
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  public record Claims(String userId, String role) {}
}
