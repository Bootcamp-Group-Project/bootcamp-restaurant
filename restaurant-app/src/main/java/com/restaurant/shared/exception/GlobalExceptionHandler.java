package com.restaurant.shared.exception;

import com.stripe.exception.StripeException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 — validation / bad input
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorBody> badRequest(RuntimeException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Validation failed");
        return body(HttpStatus.BAD_REQUEST, msg);
    }

    // Login-specific
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorBody> loginBadRequest(BadRequestException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorBody> conflict(ConflictException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorBody> unauthorized(UnauthorizedException ex) {
        return body(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorBody> notFound(ResourceNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorBody> noStaticResource(NoResourceFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 403
    @ExceptionHandler(OrderWindowClosedException.class)
    public ResponseEntity<ErrorBody> orderWindowClosed(OrderWindowClosedException ex) {
        return body(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorBody> illegalState(IllegalStateException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Feign — mirrors the downstream HTTP status back to the caller
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorBody> feign(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) status = HttpStatus.BAD_GATEWAY;
        String msg = ex.contentUTF8().isBlank() ? ex.getMessage() : ex.contentUTF8();
        return body(status, msg);
    }

    // 502
    @ExceptionHandler(StripeException.class)
    public ResponseEntity<ErrorBody> stripe(StripeException ex) {
        return body(HttpStatus.BAD_GATEWAY, "Stripe error: " + ex.getMessage());
    }

    // 500 fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> general(Exception ex) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "系統錯誤：" + ex.getMessage());
    }

    private ResponseEntity<ErrorBody> body(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorBody(status.value(), message, LocalDateTime.now()));
    }

    public record ErrorBody(int status, String message, LocalDateTime timestamp) {}
}
