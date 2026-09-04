package io.github.thena3ik.airalertmonitor.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    @Value("${air-alert.rate-limit.enabled}")
    private boolean rateLimitEnabled;

    @Value("${air-alert.rate-limit.requests-per-minute}")
    private int requestsPerMinute;

    private final ConcurrentMap<String, Bucket> bucketsByIp = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (!rateLimitEnabled) {
            return true;
        }

        String clientIp = resolveClientIp(request);
        Bucket bucket = bucketsByIp.computeIfAbsent(clientIp, ip -> newBucket(requestsPerMinute));

        if (bucket.tryConsume(1)) {
            return true;
        }

        response.setStatus(429);
        response.setHeader("Retry-After", "60");
        log.warn("Rate limit exceeded for IP {}", clientIp);
        return false;
    }

    private Bucket newBucket(int capacity) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}