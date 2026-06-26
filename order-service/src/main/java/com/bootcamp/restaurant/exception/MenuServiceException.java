package com.bootcamp.restaurant.exception;

public class MenuServiceException extends RuntimeException {
    public MenuServiceException(String message) {
        super(message);
    }
}
