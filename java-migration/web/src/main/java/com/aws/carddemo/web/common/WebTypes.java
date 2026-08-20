package com.aws.carddemo.web.common;

public final class WebTypes {
  private WebTypes() {}

  public record Context(
      String fromProgram,
      String toProgram,
      String fromTransaction,
      String toTransaction,
      String userId,
      String userType,
      Long accountId,
      String cardNumber) {}

  public record FieldError(String field, String message) {}

  public record Response<T>(
      T data, String message, String errorField, String nextRoute, Context context) {
    public static <T> Response<T> ok(T data, String nextRoute, Context context) {
      return new Response<>(data, "", "", nextRoute, context);
    }

    public static <T> Response<T> error(String message, String field, Context context) {
      return new Response<>(null, message, field, "", context);
    }
  }
}
