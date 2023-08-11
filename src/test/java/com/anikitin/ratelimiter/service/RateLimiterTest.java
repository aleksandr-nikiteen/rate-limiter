package com.anikitin.ratelimiter.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@AutoConfigureMockMvc
@SpringJUnitConfig
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public class RateLimiterTest {
    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:6-alpine").withExposedPorts(6379);
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void setUp() {
        redisContainer.start();
    }

    private static RequestPostProcessor remoteAddr(final String remoteAddr) {
        return mockRequest -> {
            mockRequest.setRemoteAddr(remoteAddr);
            return mockRequest;
        };
    }

    @Test
    public void concurrentRateLimitTest() throws InterruptedException {
        var logCaptor = new LogCaptor(RateLimitChecker.class);

        var containerIpAddress = redisContainer.getContainerIpAddress();
        var containerPort = redisContainer.getMappedPort(6379);

        var lettuceConnectionFactory = new LettuceConnectionFactory(new RedisStandaloneConfiguration(containerIpAddress, containerPort));
        lettuceConnectionFactory.afterPropertiesSet();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 30; i++) {
            executorService.execute(() -> {
                try {
                    String remoteAddr = "192.168.1.1_" + Thread.currentThread();
                    MockHttpServletRequestBuilder builder = get("/return-empty").with(remoteAddr(remoteAddr));
                    mockMvc.perform(builder);
                } catch (Exception e) {
                    /*IGNORE*/
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        assertEquals(10, logCaptor.getEvents().size());
    }
}

class LogCaptor {
    private final ListAppender<ILoggingEvent> listAppender;

    public LogCaptor(Class<?> clazz) {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(clazz);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    public List<ILoggingEvent> getEvents() {
        return listAppender.list;
    }

    public void clearEvents() {
        listAppender.list.clear();
    }
}
