package com.cognition.usersecurity.exception;

import java.util.Map;
import java.util.LinkedHashMap;
import com.cognition.usersecurity.dto.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiErrorResponse> api(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiErrorResponse(ex.getMessage(), null));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiErrorResponse("Validation failed", errors));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> illegal(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(ex.getMessage(), null));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> unreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse("Malformed request or invalid user type", null));
    }
}
