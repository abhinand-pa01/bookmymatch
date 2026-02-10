package com.abhinand.bookmymatch.config;

import com.abhinand.bookmymatch.entity.User;
import com.abhinand.bookmymatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.initial.enabled:false}")
    private boolean adminInitEnabled;

    @Value("${admin.initial.username:}")
    private String adminUsername;

    @Value("${admin.initial.password:}")
    private String adminPassword;

    @Value("${admin.force.password.change:true}")
    private boolean forcePasswordChange;

    @Override
    public void run(String... args) {


        if (!adminInitEnabled) {
            log.info("Admin initializer is disabled");
            return;
        }


        if (!StringUtils.hasText(adminUsername) || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                    "Admin initialization enabled but username/password not provided"
            );
        }

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin user '{}' already exists. Initializer skipped.", adminUsername);
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .fullName("System Administrator")
                .email("admin@bookmymatch.com")
                .role(User.Role.ROLE_ADMIN)
                .enabled(true)
                .mustChangePassword(forcePasswordChange)
                .build();

        userRepository.save(admin);

        log.info("═══════════════════════════════════════════════════════════════");
        log.info("  Admin user created successfully");
        log.info("  Username: {}", adminUsername);
        log.warn("  Password was provided via secure configuration");
        log.warn("  Password change is REQUIRED on first login");
        log.info("═══════════════════════════════════════════════════════════════");
    }
}
