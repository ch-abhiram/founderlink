package com.auth_service.Util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    @Test
    void generateTokenCanBeParsedForEmailAndRemainingTime() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "01234567890123456789012345678901");
        jwtUtil.init();

        String token = jwtUtil.generateToken("user@test.com", "ROLE_FOUNDER");

        assertEquals("user@test.com", jwtUtil.extractEmail(token));
        assertTrue(jwtUtil.getRemainingTime(token) > 0);
    }

    @Test
    void initRejectsBlankSecret() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", " ");

        assertThrows(IllegalStateException.class, jwtUtil::init);
    }
}
