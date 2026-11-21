
package com.hardwarestore.erp.auth.service;

import com.hardwarestore.erp.auth.entity.AppUser;
import com.hardwarestore.erp.auth.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(
        name = "app.bootstrap.admin.enabled",
        havingValue = "true"
)
public class AdminBootstrap implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.display-name:}")
    private String adminDisplayName;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    public AdminBootstrap(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (adminEmail.isBlank()
                || adminDisplayName.isBlank()
                || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Admin bootstrap is enabled but required credentials are missing"
            );
        }

        if (adminPassword.length() < 12) {
            throw new IllegalStateException(
                    "Initial Admin password must contain at least 12 characters"
            );
        }

        if (userRepository.count() > 0) {
            System.out.println(
                    "Admin bootstrap skipped: user accounts already exist."
            );
            return;
        }

        AppUser admin = AppUser.createAdmin(
                adminEmail,
                adminDisplayName,
                passwordEncoder.encode(adminPassword)
        );

        userRepository.save(admin);

        System.out.println("Initial Admin account created successfully.");
    }
}
