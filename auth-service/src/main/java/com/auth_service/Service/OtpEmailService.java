package com.auth_service.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${founderlink.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${founderlink.mail.from:no-reply@founderlink.local}")
    private String fromEmail;

    @Value("${founderlink.otp.expiry-minutes:10}")
    private long otpExpiryMinutes;

    public void sendOtp(String toEmail, String otp) {
        if (!mailEnabled) {
            log.info("[DEV] OTP for email={} is: {}", toEmail, otp);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Mail enabled but JavaMailSender not configured. OTP for email={}: {}", toEmail, otp);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your FounderLink verification code");
            message.setText(buildOtpBody(otp));
            mailSender.send(message);
            log.info("OTP email sent to email={}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to email={}: {}. [DEV FALLBACK] OTP is: {}", toEmail, ex.getMessage(), otp);
        }
    }

    public void sendPasswordResetOtp(String toEmail, String otp) {
        if (!mailEnabled) {
            log.info("[DEV] Password reset OTP for email={} is: {}", toEmail, otp);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Mail enabled but JavaMailSender not configured. Password reset OTP for email={}: {}", toEmail, otp);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your FounderLink password reset code");
            message.setText(buildPasswordResetBody(otp));
            mailSender.send(message);
            log.info("Password reset OTP email sent to email={}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send password reset OTP email to email={}: {}. [DEV FALLBACK] OTP is: {}", toEmail, ex.getMessage(), otp);
        }
    }

    private String buildOtpBody(String otp) {
        return "Welcome to FounderLink!\n\n"
             + "Your email verification code is:\n\n"
             + "    " + otp + "\n\n"
             + "This code expires in " + otpExpiryMinutes + " minutes.\n\n"
             + "If you did not create a FounderLink account, you can safely ignore this email.\n\n"
             + "- The FounderLink Team";
    }

    private String buildPasswordResetBody(String otp) {
        return "FounderLink password reset\n\n"
             + "Use this code to reset your password:\n\n"
             + "    " + otp + "\n\n"
             + "This code expires in " + otpExpiryMinutes + " minutes.\n\n"
             + "If you did not request a password reset, you can ignore this email.\n\n"
             + "- The FounderLink Team";
    }
}
