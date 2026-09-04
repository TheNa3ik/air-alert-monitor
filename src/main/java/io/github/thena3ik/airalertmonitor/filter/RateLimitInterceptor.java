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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";
    public static final String TRUSTED_CALLER_ATTR = "TRUSTED_CALLER";

    @Value("${air-alert.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${air-alert.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${air-alert.rate-limit.trusted-requests-per-minute:200}")
    private int trustedRequestsPerMinute;

    @Value("${air-alert.internal-api-key:}")
    private String internalApiKey;

    private final ConcurrentMap<String, Bucket> bucketsByIp = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Bucket> trustedBuckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (!rateLimitEnabled) {
            return true;
        }

        if (isTrustedRequest(request)) {
            request.setAttribute(TRUSTED_CALLER_ATTR, true);
            Bucket bucket = trustedBuckets.computeIfAbsent(internalApiKey, key -> newBucket(trustedRequestsPerMinute));
            return consumeOrReject(bucket, response, "trusted caller");
        }

        String clientIp = resolveClientIp(request);
        Bucket bucket = bucketsByIp.computeIfAbsent(clientIp, ip -> newBucket(requestsPerMinute));
        return consumeOrReject(bucket, response, clientIp);
    }

    private boolean isTrustedRequest(HttpServletRequest request) {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            return false;
        }
        String providedKey = request.getHeader(API_KEY_HEADER);
        if (providedKey == null) {
            return false;
        }
        return MessageDigest.isEqual(
                internalApiKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8));
    }

    private boolean consumeOrReject(Bucket bucket, HttpServletResponse response, String identifier) {
        if (bucket.tryConsume(1)) {
            return true;
        }
        response.setStatus(429);
        response.setHeader("Retry-After", "60");
        log.warn("Rate limit exceeded for {}", identifier);
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