package com.restaurant.shared.exception;

public class OrderWindowClosedException extends RuntimeException {
    public OrderWindowClosedException(String message) { super(message); }
}
