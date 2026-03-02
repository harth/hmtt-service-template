package uk.co.hmtt.template.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** Standard error envelope returned by {@link GlobalExceptionHandler}. */
@Schema(description = "Standard error response body")
public record ErrorResponse(
    @Schema(description = "ISO-8601 timestamp of the error", example = "2025-01-01T12:00:00Z")
        Instant timestamp,
    @Schema(description = "HTTP status code", example = "404") int status,
    @Schema(description = "Human-readable error message", example = "Item not found")
        String message,
    @Schema(description = "Request path that triggered the error", example = "/items/99")
        String path) {}
