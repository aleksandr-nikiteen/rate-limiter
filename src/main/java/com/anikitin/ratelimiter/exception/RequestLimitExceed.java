package com.anikitin.ratelimiter.exception;

public class RequestLimitExceed extends RuntimeException {
    public RequestLimitExceed(String message) {
        super(message);
    }
}
