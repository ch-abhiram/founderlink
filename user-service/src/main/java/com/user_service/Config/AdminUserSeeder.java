package com.user_service.Config;

import com.user_service.Entity.User;
import com.user_service.Repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder implements ApplicationRunner {

    public static final String ADMIN_EMAIL = "admin@founderlink.local";

    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public AdminUserSeeder(ObjectProvider<UserRepository> userRepositoryProvider) {
        this.userRepositoryProvider = userRepositoryProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        UserRepository userRepository = userRepositoryProvider.getIfAvailable();
        if (userRepository == null) {
            return;
        }

        User admin = userRepository.findByEmail(ADMIN_EMAIL).orElseGet(User::new);
        admin.setEmail(ADMIN_EMAIL);
        admin.setName("FounderLink Admin");
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
    }
}
