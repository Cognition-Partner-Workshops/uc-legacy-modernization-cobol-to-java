package com.carddemo.usersecurity.web;

import com.carddemo.usersecurity.dto.ErrorResponse;
import com.carddemo.usersecurity.service.UserAlreadyExistsException;
import com.carddemo.usersecurity.service.UserNotFoundException;
import com.carddemo.usersecurity.service.WrongPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Translates the legacy error conditions into HTTP status codes. */
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
        return status(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ErrorResponse> handleWrongPassword(WrongPasswordException ex) {
        return status(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(UserAlreadyExistsException ex) {
        return status(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");
        return status(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseEntity<ErrorResponse> status(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }
}
