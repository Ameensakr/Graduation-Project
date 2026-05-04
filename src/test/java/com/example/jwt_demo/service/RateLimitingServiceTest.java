package com.example.jwt_demo.service;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingServiceTest {

    private final RateLimitingService service = new RateLimitingService();

    @Test
    void forgetBucket_allowsThreeThenBlocks() {
        Bucket b = service.resolveForgetBucket("user@example.com");

        assertThat(b.tryConsume(1)).isTrue();
        assertThat(b.tryConsume(1)).isTrue();
        assertThat(b.tryConsume(1)).isTrue();
        assertThat(b.tryConsume(1)).isFalse();
    }

    @Test
    void forgetBucket_perEmailIsolation() {
        Bucket a = service.resolveForgetBucket("a@x.com");
        Bucket c = service.resolveForgetBucket("c@x.com");

        a.tryConsume(3);
        assertThat(a.tryConsume(1)).isFalse();
        assertThat(c.tryConsume(1)).isTrue();
    }

    @Test
    void forgetBucket_sameEmailReturnsSameBucket() {
        Bucket b1 = service.resolveForgetBucket("a@x.com");
        Bucket b2 = service.resolveForgetBucket("a@x.com");
        assertThat(b1).isSameAs(b2);
    }

    @Test
    void resetBucket_allowsFiveThenBlocks() {
        Bucket b = service.resolveResetBucket("user@example.com");
        for (int i = 0; i < 5; i++) {
            assertThat(b.tryConsume(1)).isTrue();
        }
        assertThat(b.tryConsume(1)).isFalse();
    }
}
