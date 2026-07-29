package com.cognition.usersecurity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public record ApiErrorResponse(
        @Schema(example = "User ID NOT found...") String error,
        Map<String, String> fields) {}
