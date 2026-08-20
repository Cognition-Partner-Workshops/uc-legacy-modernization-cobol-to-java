package com.carddemo.web.signon;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SignonController {
    private final SignonService signonService;

    public SignonController(SignonService signonService) {
        this.signonService = signonService;
    }

    @PostMapping("/signon")
    public SignonResponse signon(@RequestBody SignonRequest request) {
        return signonService.signon(request.userId(), request.password());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(SignonFailureException.class)
    public ResponseEntity<ErrorResponse> handleFailure(SignonFailureException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new ErrorResponse(exception.getMessage()));
    }

    public record ErrorResponse(String message) {
    }
}
