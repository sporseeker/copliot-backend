package com.spotseeker.copliot.config;

import com.spotseeker.copliot.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${rate.limit.requests}")
    private int maxRequests;

    @Value("${rate.limit.duration}")
    private long durationMs;

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientId = getClientIdentifier(request);
        RequestCounter counter = requestCounts.computeIfAbsent(clientId, k -> new RequestCounter());

        long currentTime = System.currentTimeMillis();

        synchronized (counter) {
            if (currentTime - counter.getStartTime() > durationMs) {
                counter.reset(currentTime);
            }

            if (counter.getCount() >= maxRequests) {
                throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
            }

            counter.increment();
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    private static class RequestCounter {
        private AtomicInteger count = new AtomicInteger(0);
        private long startTime;

        public RequestCounter() {
            this.startTime = System.currentTimeMillis();
        }

        public int getCount() {
            return count.get();
        }

        public void increment() {
            count.incrementAndGet();
        }

        public long getStartTime() {
            return startTime;
        }

        public void reset(long newStartTime) {
            count.set(0);
            startTime = newStartTime;
        }
    }
}
