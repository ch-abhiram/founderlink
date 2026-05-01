package com.auth_service.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisService service;

    @BeforeEach
    void setUp() {
        service = new RedisService(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void tokenBlacklistUsesTokenKeyWithMillisExpiry() {
        service.blacklistToken("access", 5000L);
        verify(valueOperations).set("access", "blacklisted", 5000L, TimeUnit.MILLISECONDS);

        when(redisTemplate.hasKey("access")).thenReturn(true);
        assertTrue(service.isBlacklisted("access"));

        when(redisTemplate.hasKey("missing")).thenReturn(false);
        assertFalse(service.isBlacklisted("missing"));
    }

    @Test
    void otpLifecycleUsesExpectedKeys() {
        service.storeOtp("user@test.com", "123456", 10);
        verify(valueOperations).set("otp:user@test.com", "123456", 10, TimeUnit.MINUTES);

        when(valueOperations.get("otp:user@test.com")).thenReturn("123456");
        assertEquals("123456", service.getOtp("user@test.com"));

        service.deleteOtp("user@test.com");
        verify(redisTemplate).delete("otp:user@test.com");
    }

    @Test
    void otpAttemptsExpireOnFirstIncrementAndDefaultToOneWhenRedisReturnsNull() {
        when(valueOperations.increment("otp_attempts:user@test.com")).thenReturn(1L, null);

        assertEquals(1, service.incrementOtpAttempts("user@test.com"));
        verify(redisTemplate).expire("otp_attempts:user@test.com", 10, TimeUnit.MINUTES);
        assertEquals(1, service.incrementOtpAttempts("user@test.com"));

        service.clearOtpAttempts("user@test.com");
        verify(redisTemplate).delete("otp_attempts:user@test.com");
    }

    @Test
    void cooldownAndResetTokenOperationsUseNamespacedKeys() {
        when(redisTemplate.hasKey("otp_cooldown:user@test.com")).thenReturn(true);
        assertTrue(service.isOtpCooldownActive("user@test.com"));

        service.setOtpCooldown("user@test.com");
        verify(valueOperations).set("otp_cooldown:user@test.com", "1", 2, TimeUnit.MINUTES);

        service.storeResetToken("user@test.com", "reset", 15);
        verify(valueOperations).set("reset:user@test.com", "reset", 15, TimeUnit.MINUTES);
        when(valueOperations.get("reset:user@test.com")).thenReturn("reset");
        assertEquals("reset", service.getResetToken("user@test.com"));

        when(valueOperations.increment("reset_attempts:user@test.com")).thenReturn(2L);
        assertEquals(2, service.incrementResetAttempts("user@test.com"));

        service.deleteResetToken("user@test.com");
        service.clearResetAttempts("user@test.com");
        verify(redisTemplate).delete("reset:user@test.com");
        verify(redisTemplate).delete("reset_attempts:user@test.com");
    }
}
