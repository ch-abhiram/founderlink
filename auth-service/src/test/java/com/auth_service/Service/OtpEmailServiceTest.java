package com.auth_service.Service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OtpEmailServiceTest {

    @Mock
    private ObjectProvider<JavaMailSender> provider;

    @Mock
    private JavaMailSender mailSender;

    private OtpEmailService service;

    @BeforeEach
    void setUp() {
        service = new OtpEmailService(provider);
        ReflectionTestUtils.setField(service, "fromEmail", "no-reply@test.com");
        ReflectionTestUtils.setField(service, "otpExpiryMinutes", 10L);
    }

    @Test
    void disabledMailLogsOtpWithoutUsingProvider() {
        ReflectionTestUtils.setField(service, "mailEnabled", false);

        service.sendOtp("user@test.com", "123456");
        service.sendPasswordResetOtp("user@test.com", "654321");

        verify(provider, never()).getIfAvailable();
    }

    @Test
    void enabledMailSendsVerificationAndPasswordResetMessages() {
        ReflectionTestUtils.setField(service, "mailEnabled", true);
        when(provider.getIfAvailable()).thenReturn(mailSender);

        service.sendOtp("user@test.com", "123456");
        service.sendPasswordResetOtp("user@test.com", "654321");

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void enabledMailWithoutSenderFallsBackToLogging() {
        ReflectionTestUtils.setField(service, "mailEnabled", true);
        when(provider.getIfAvailable()).thenReturn(null);

        service.sendOtp("user@test.com", "123456");
        service.sendPasswordResetOtp("user@test.com", "654321");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
