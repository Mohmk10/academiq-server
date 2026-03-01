package com.academiq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${CORS_ALLOWED_ORIGINS:}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = buildAllowedOrigins();

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    private String[] buildAllowedOrigins() {
        String devOrigin = "http://localhost:4200";

        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            return new String[]{devOrigin};
        }

        String[] extraOrigins = allowedOrigins.split(",");
        String[] allOrigins = new String[extraOrigins.length + 1];
        allOrigins[0] = devOrigin;
        System.arraycopy(extraOrigins, 0, allOrigins, 1, extraOrigins.length);

        return allOrigins;
    }
}
