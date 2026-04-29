package com.carddemo.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<FieldError> fieldErrors;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FieldError {
        private final String field;
        private final String message;
    }
}
