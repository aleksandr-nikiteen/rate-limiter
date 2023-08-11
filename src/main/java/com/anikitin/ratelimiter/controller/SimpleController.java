package com.anikitin.ratelimiter.controller;


import com.anikitin.ratelimiter.aop.RateLimit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Simple controller that return empty response
 */
@RestController
public class SimpleController {
    @RateLimit
    @GetMapping("/return-empty")
    public ResponseEntity<String> returnEmpty() {
        return new ResponseEntity<>("", HttpStatus.OK);
    }
}
