package com.academiq.config;

import com.academiq.service.RegleAlerteService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RegleAlerteService regleAlerteService;

    @Override
    public void run(String... args) {
        regleAlerteService.initialiserReglesParDefaut();
        log.info("Initialisation des données terminée");
    }
}
