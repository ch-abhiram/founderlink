package com.auth_service.Service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_ATTEMPT_PREFIX = "otp_attempts:";
    private static final String OTP_COOLDOWN_PREFIX = "otp_cooldown:";
    private static final String RESET_PREFIX = "reset:";
    private static final String RESET_ATTEMPT_PREFIX = "reset_attempts:";

    private final StringRedisTemplate redisTemplate;

    public void blacklistToken(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(token, "blacklisted", expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }

    public void storeOtp(String email, String otp, long expiryMinutes) {
        redisTemplate.opsForValue().set(OTP_PREFIX + email, otp, expiryMinutes, TimeUnit.MINUTES);
    }

    public String getOtp(String email) {
        return redisTemplate.opsForValue().get(OTP_PREFIX + email);
    }

    public void deleteOtp(String email) {
        redisTemplate.delete(OTP_PREFIX + email);
    }

    public int incrementOtpAttempts(String email) {
        String key = OTP_ATTEMPT_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }
        return count != null ? count.intValue() : 1;
    }

    public void clearOtpAttempts(String email) {
        redisTemplate.delete(OTP_ATTEMPT_PREFIX + email);
    }

    public boolean isOtpCooldownActive(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(OTP_COOLDOWN_PREFIX + email));
    }

    public void setOtpCooldown(String email) {
        redisTemplate.opsForValue().set(OTP_COOLDOWN_PREFIX + email, "1", 2, TimeUnit.MINUTES);
    }

    public void storeResetToken(String email, String token, long expiryMinutes) {
        redisTemplate.opsForValue().set(RESET_PREFIX + email, token, expiryMinutes, TimeUnit.MINUTES);
    }

    public String getResetToken(String email) {
        return redisTemplate.opsForValue().get(RESET_PREFIX + email);
    }

    public void deleteResetToken(String email) {
        redisTemplate.delete(RESET_PREFIX + email);
    }

    public int incrementResetAttempts(String email) {
        String key = RESET_ATTEMPT_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }
        return count != null ? count.intValue() : 1;
    }

    public void clearResetAttempts(String email) {
        redisTemplate.delete(RESET_ATTEMPT_PREFIX + email);
    }
}
