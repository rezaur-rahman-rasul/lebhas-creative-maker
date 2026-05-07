package com.lebhas.creativesaas.common.security.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class RedisAccessTokenRevocationStore implements AccessTokenRevocationStore {

    private final StringRedisTemplate redisTemplate;
    private final Clock clock;
    private final String namespace;

    public RedisAccessTokenRevocationStore(
            StringRedisTemplate redisTemplate,
            Clock clock,
            @Value("${platform.redis.token-namespace:creative-saas:tokens}") String namespace
    ) {
        this.redisTemplate = redisTemplate;
        this.clock = clock;
        this.namespace = namespace;
    }

    @Override
    public void revoke(String tokenId, Instant expiresAt) {
        Duration ttl = Duration.between(clock.instant(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redisTemplate.opsForValue().set(key(tokenId), "revoked", ttl);
    }

    @Override
    public boolean isRevoked(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(tokenId)));
    }

    private String key(String tokenId) {
        return namespace + ":revoked:" + tokenId;
    }
}
