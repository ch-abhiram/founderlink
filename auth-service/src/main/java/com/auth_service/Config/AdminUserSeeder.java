package com.auth_service.Config;

import com.auth_service.Entity.User;
import com.auth_service.Repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder implements ApplicationRunner {

    public static final String ADMIN_EMAIL = "admin@founderlink.local";
    public static final String ADMIN_PASSWORD = "Admin@12345";

    private final ObjectProvider<UserRepository> userRepositoryProvider;
    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    public AdminUserSeeder(
            ObjectProvider<UserRepository> userRepositoryProvider,
            ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
        this.userRepositoryProvider = userRepositoryProvider;
        this.passwordEncoderProvider = passwordEncoderProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        UserRepository userRepository = userRepositoryProvider.getIfAvailable();
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();

        if (userRepository == null || passwordEncoder == null) {
            return;
        }

        User admin = userRepository.findByEmail(ADMIN_EMAIL).orElseGet(User::new);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRole("ROLE_ADMIN");
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        userRepository.save(admin);
    }
}
