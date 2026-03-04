package com.academiq.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class PasswordHashGenerator implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(PasswordHashGenerator.class);

    @Override
    public void run(String... args) {
        log.info("=== HASH POUR SUPER_ADMIN ===");
        log.info("SuperAdmin@2026 → {}", passwordEncoder.encode("SuperAdmin@2026"));
        log.info("=============================");
    }
}
