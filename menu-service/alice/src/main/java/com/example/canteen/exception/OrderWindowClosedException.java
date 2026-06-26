package com.example.canteen.exception;

public class OrderWindowClosedException extends RuntimeException {

    public OrderWindowClosedException(String message) {
        super(message);
    }
}
