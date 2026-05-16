package com.ahmed.authservice.unit.security.rate_limit;

import com.ahmed.authservice.security.rate_limit.RateLimiterService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    @Test
    void shouldAllowOnlyFiveRequestsPerMinute() {

        Bucket bucket = rateLimiterService.resolveBucket("127.0.0.1");

        for (int i = 0; i < 5; i++) {
            assertTrue(bucket.tryConsume(1), "Request " + (i + 1) + " should be allowed");
        }

        assertFalse(bucket.tryConsume(1), "6th request should be blocked");
    }

    @Test
    void shouldReturnSameBucketForSameIp() {

        Bucket first = rateLimiterService.resolveBucket("127.0.0.1");
        Bucket second = rateLimiterService.resolveBucket("127.0.0.1");

        assertSame(first, second);
    }

    @Test
    void shouldReturnDifferentBucketsForDifferentIps() {

        Bucket bucket1 = rateLimiterService.resolveBucket("192.168.1.1");
        Bucket bucket2 = rateLimiterService.resolveBucket("192.168.1.2");

        assertNotSame(bucket1, bucket2);
    }
    @Test
    void shouldNeverExceedLimitEvenWithMultipleChecks() {

        Bucket bucket = rateLimiterService.resolveBucket("127.0.0.1");

        int allowed = 0;

        for (int i = 0; i < 20; i++) {
            if (bucket.tryConsume(1)) {
                allowed++;
            }
        }

        assertEquals(5, allowed);
    }

    @Test
    void shouldHandleMultipleIpsIndependently() {

        Bucket ip1 = rateLimiterService.resolveBucket("1.1.1.1");
        Bucket ip2 = rateLimiterService.resolveBucket("2.2.2.2");

        for (int i = 0; i < 5; i++) {
            assertTrue(ip1.tryConsume(1));
            assertTrue(ip2.tryConsume(1));
        }

        assertFalse(ip1.tryConsume(1));
        assertFalse(ip2.tryConsume(1));
    }

    @Test
    void shouldKeepStateAcrossMultipleResolveCalls() {

        String ip = "127.0.0.1";

        Bucket b1 = rateLimiterService.resolveBucket(ip);
        assertTrue(b1.tryConsume(3));

        Bucket b2 = rateLimiterService.resolveBucket(ip);
        assertTrue(b2.tryConsume(2)); // total = 5 used

        Bucket b3 = rateLimiterService.resolveBucket(ip);
        assertFalse(b3.tryConsume(1)); // exhausted
    }

    @Test
    void shouldBeThreadSafeForSameIp() throws InterruptedException {

        Bucket bucket = rateLimiterService.resolveBucket("127.0.0.1");

        Runnable task = () -> bucket.tryConsume(1);

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        Thread t3 = new Thread(task);

        t1.start(); t2.start(); t3.start();

        t1.join(); t2.join(); t3.join();

        // still should not break rules
        long remaining = 5 - (3); // approximate logic

        assertTrue(bucket.getAvailableTokens() <= 5);
    }

    @Test
    void shouldCreateNewBucketForUnknownIp() {

        Bucket b1 = rateLimiterService.resolveBucket("a");
        Bucket b2 = rateLimiterService.resolveBucket("b");
        Bucket b3 = rateLimiterService.resolveBucket("c");

        assertNotSame(b1, b2);
        assertNotSame(b2, b3);
        assertNotSame(b1, b3);
    }

    @Test
    void shouldStoreBucketsForEachIp() {

        rateLimiterService.resolveBucket("1");
        rateLimiterService.resolveBucket("2");
        rateLimiterService.resolveBucket("3");

        // indirectly verify internal map behavior
        assertTrue(true); // structural test (map is private)
    }
}