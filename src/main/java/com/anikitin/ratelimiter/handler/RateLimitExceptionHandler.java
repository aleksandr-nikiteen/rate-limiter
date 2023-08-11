package com.anikitin.ratelimiter.handler;


import com.anikitin.ratelimiter.exception.RequestLimitExceed;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RateLimitExceptionHandler {

    @ExceptionHandler(value = RequestLimitExceed.class)
    public ResponseEntity<Object> exception(RequestLimitExceed exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_GATEWAY);
    }
}
