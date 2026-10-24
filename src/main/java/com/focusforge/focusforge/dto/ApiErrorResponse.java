
package com.focusforge.focusforge.dto;

import java.time.LocalDateTime;

public class ApiErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    public ApiErrorResponse(
            int status,
            String error,
            String message,
            String path) {

        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
