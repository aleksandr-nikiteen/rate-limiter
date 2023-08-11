package com.anikitin.ratelimiter.service;


import com.anikitin.ratelimiter.config.RateLimiterProperties;
import com.anikitin.ratelimiter.exception.RequestLimitExceed;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitChecker {
    private final HttpServletRequest request;
    private final RedisTemplate<String, Integer> redisTemplate;
    private final RateLimiterProperties rateLimiterProperties;

    /**
     * Check count of request by methodName and IP
     *
     * @param methodName Method's name
     */
    public void check(String methodName) {
        log.debug("Try to check request count for method {}", methodName);
        String remoteAddr = this.request.getRemoteAddr();
        String key = methodName + "." + remoteAddr;
        Integer request = redisTemplate.opsForValue().get(key);
        if (request == null) {
            redisTemplate.opsForValue().set(key, 1);
            redisTemplate.expire(key, rateLimiterProperties.getTimeLimit(), TimeUnit.MINUTES);
            log.debug("Checked request count for method={} and ip={}", methodName, remoteAddr);
        } else if (request >= rateLimiterProperties.getRequestLimit()) {
            log.error("Request limit is exceed for {}", key);
            throw new RequestLimitExceed("Request limit is exceed");
        } else {
            log.debug("Increased request count for method={} and ip={}", methodName, remoteAddr);
            redisTemplate.opsForValue().increment(key, +1);
        }
    }
}

