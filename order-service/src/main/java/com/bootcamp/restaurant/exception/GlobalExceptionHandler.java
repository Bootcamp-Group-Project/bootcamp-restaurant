package com.bootcamp.restaurant.exception;

import com.bootcamp.restaurant.dto.ErrorResp;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResp handleNotFound(ResourceNotFoundException ex) {
        return ErrorResp.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResp handleBadRequest(IllegalArgumentException ex) {
        return ErrorResp.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MenuServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResp handleMenuService(MenuServiceException ex) {
        return ErrorResp.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(PickupServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResp handlePickupService(PickupServiceException ex) {
        return ErrorResp.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(WalletServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResp handleWalletService(WalletServiceException ex) {
        return ErrorResp.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResp handleGeneral(RuntimeException ex) {
        return ErrorResp.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
