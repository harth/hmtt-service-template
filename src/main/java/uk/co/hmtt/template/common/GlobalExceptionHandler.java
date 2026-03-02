package uk.co.hmtt.template.common;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralised exception-to-HTTP-response mapping. All 4xx errors are logged at WARN level; 5xx
 * errors are logged at ERROR level.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleEntityNotFoundException(
      final EntityNotFoundException ex, final HttpServletRequest request) {
    log.warn("Entity not found at {}: {}", request.getRequestURI(), ex.getMessage());
    return new ErrorResponse(
        Instant.now(), HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleValidationException(
      final MethodArgumentNotValidException ex, final HttpServletRequest request) {
    final String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining(", "));
    log.warn("Validation failed at {}: {}", request.getRequestURI(), message);
    return new ErrorResponse(
        Instant.now(), HttpStatus.BAD_REQUEST.value(), message, request.getRequestURI());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleMessageNotReadable(
      final HttpMessageNotReadableException ex, final HttpServletRequest request) {
    log.warn("Malformed request body at {}: {}", request.getRequestURI(), ex.getMessage());
    return new ErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Malformed or unreadable request body",
        request.getRequestURI());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public ErrorResponse handleMethodNotSupported(
      final HttpRequestMethodNotSupportedException ex, final HttpServletRequest request) {
    log.warn("Method not allowed at {}: {}", request.getRequestURI(), ex.getMessage());
    return new ErrorResponse(
        Instant.now(),
        HttpStatus.METHOD_NOT_ALLOWED.value(),
        ex.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleGenericException(
      final Exception ex, final HttpServletRequest request) {
    log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return new ErrorResponse(
        Instant.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "An unexpected error occurred",
        request.getRequestURI());
  }
}
