package com.carddemo.usersecurity.dto;

/** Error payload carrying the message text used by the legacy BMS screens. */
public record ErrorResponse(int status, String message) {
}
