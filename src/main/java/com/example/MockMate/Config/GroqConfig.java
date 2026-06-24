package com.example.MockMate.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class GroqConfig {

    @Value("${groq.api.key}")
    private String apiKey;

    // ── Creates a RestClient bean pre-configured for Groq ─────────────────
    // baseUrl and Authorization header set once here
    // Injected wherever needed via @RequiredArgsConstructor
    @Bean
    public RestClient groqRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
    // ── Registers ObjectMapper as a Spring bean ───────────────────────────
    // Required by QuestionGenerationService for JSON parsing
    // Spring Boot doesn't auto-register this unless you use @EnableAutoConfiguration
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
