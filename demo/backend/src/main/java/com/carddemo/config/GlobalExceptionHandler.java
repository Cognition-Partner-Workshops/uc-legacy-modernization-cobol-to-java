package com.carddemo.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Global exception handler — equivalent to COBOL ABEND-ROUTINE / 9999-ABEND-ROUTINE
 * in COCRDUPC.cbl and COCRDSLC.cbl.
 *
 * COBOL catches file status errors (VSAM I/O), ABEND conditions, and validation
 * failures, formatting them into BMS error message fields (ERRMSGO).
 * This handler provides equivalent structured error responses for the REST API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Equivalent to BMS ERRMSGO field population in COBOL programs
    // COBOL sets WS-MESSAGE with descriptive text and sends via SEND MAP.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "error", "Validation Error",
                "message", ex.getMessage()
        ));
    }

    // Equivalent to 2000-DECIDE-ACTION PF5 confirmation check in COCRDUPC.cbl
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleStateError(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 409,
                "error", "State Error",
                "message", ex.getMessage()
        ));
    }

    // Equivalent to 9300-CHECK-CHANGE-IN-REC in COCRDUPC.cbl
    // COBOL compares before/after field values to detect concurrent modification.
    // JPA @Version triggers this exception when another transaction modified the record.
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleConcurrencyConflict(
            ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 409,
                "error", "Concurrent Modification",
                "message", "Record was modified by another user. Please refresh and try again."
        ));
    }

    // Equivalent to 9999-ABEND-ROUTINE in COCRDUPC.cbl / COCRDSLC.cbl
    // COBOL: EXEC CICS ABEND ABCODE('CCRD') → log and terminate.
    // Here we return a 500 with structured error info instead of abending.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 500,
                "error", "Internal Server Error",
                "message", "An unexpected error occurred. Please contact support."
        ));
    }
}
