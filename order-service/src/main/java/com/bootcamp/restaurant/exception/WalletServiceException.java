package com.bootcamp.restaurant.exception;

public class WalletServiceException extends RuntimeException {
    public WalletServiceException(String message) {
        super(message);
    }
}
