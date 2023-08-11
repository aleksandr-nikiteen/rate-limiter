package com.anikitin.ratelimiter.aop;

import com.anikitin.ratelimiter.service.RateLimitChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


/**
 * Proxy for method that marked by @RateLimit
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AspectLimit {
    private final RateLimitChecker rateLimitChecker;

    @Around("@annotation(com.anikitin.ratelimiter.aop.RateLimit)")
    public Object aroundCustomAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Executing proxy");
        rateLimitChecker.check(joinPoint.getSignature().getName());
        return joinPoint.proceed();
    }
}
