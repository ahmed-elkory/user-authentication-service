package com.ahmed.authservice.security.rate_limit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides rate limiting functionality using Bucket4j.

 * Limits number of requests per IP address to prevent abuse
 * such as brute-force login attempts.
 */
@Service
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Defines rate limit policy:
     * 5 requests per minute.
     */
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(5, Duration.ofMinutes(1))) // 5 requests/min
                .build();
    }

    /**
     * Resolves or creates a rate limit bucket for a given IP.
     *
     * @param ip client IP address
     * @return bucket instance
     */
    public Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, k -> createNewBucket());
    }
}
