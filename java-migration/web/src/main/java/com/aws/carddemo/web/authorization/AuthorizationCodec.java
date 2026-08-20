package com.aws.carddemo.web.authorization;

import java.math.BigDecimal;

final class AuthorizationCodec {
  private AuthorizationCodec() {}

  static AuthorizationService.Request decodeRequest(String value) {
    String body = pad(value, 145);
    int p = 0;
    String date = take(body, p, 6);
    p += 6;
    String time = take(body, p, 6);
    p += 6;
    String card = take(body, p, 16);
    p += 16;
    String type = take(body, p, 4);
    p += 4;
    String expiry = take(body, p, 4);
    p += 4;
    String messageType = take(body, p, 6);
    p += 6;
    String source = take(body, p, 6);
    p += 6;
    String processing = take(body, p, 6);
    p += 6;
    String amount = take(body, p, 14);
    p += 14;
    String mcc = take(body, p, 4);
    p += 4;
    String country = take(body, p, 3);
    p += 3;
    Integer pos = number(take(body, p, 2));
    p += 2;
    String merchantId = take(body, p, 15);
    p += 15;
    String merchantName = take(body, p, 22);
    p += 22;
    String city = take(body, p, 13);
    p += 13;
    String state = take(body, p, 2);
    p += 2;
    String zip = take(body, p, 9);
    p += 9;
    String transaction = take(body, p, 15);
    return new AuthorizationService.Request(
        date,
        time,
        card,
        type,
        expiry,
        messageType,
        source,
        processing,
        decimal(amount),
        mcc,
        country,
        pos,
        merchantId,
        merchantName,
        city,
        state,
        zip,
        transaction);
  }

  static String encodeReply(AuthorizationService.Reply reply) {
    return fixed(reply.cardNum(), 16)
        + fixed(reply.transactionId(), 15)
        + fixed(reply.authIdCode(), 6)
        + fixed(reply.authResponseCode(), 2)
        + fixed(reply.authResponseReason(), 4)
        + String.format("%14s", reply.approvedAmount().setScale(2).toPlainString());
  }

  private static String fixed(String value, int length) {
    return String.format("%-" + length + "s", value == null ? "" : value).substring(0, length);
  }

  private static String pad(String value, int length) {
    return fixed(value, length);
  }

  private static String take(String value, int start, int length) {
    return value.substring(start, start + length).trim();
  }

  private static Integer number(String value) {
    try {
      return Integer.valueOf(value);
    } catch (RuntimeException e) {
      return 0;
    }
  }

  private static BigDecimal decimal(String value) {
    try {
      return new BigDecimal(value.replace("+", "").replace(",", "").trim());
    } catch (RuntimeException e) {
      return BigDecimal.ZERO;
    }
  }
}
