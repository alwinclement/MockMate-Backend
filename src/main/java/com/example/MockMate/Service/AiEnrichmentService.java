package com.example.MockMate.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.List;
import java.util.Map;

@Service
public class AiEnrichmentService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.base-url}")
    private String groqBaseUrl;

    @Value("${groq.model}")
    private String groqModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestClient buildClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        return RestClient.builder().requestFactory(factory).build();
    }

    public AiResumeResult enrich(String rawText, List<String> regexSkills) throws Exception {
        String prompt = buildPrompt(rawText, regexSkills);

        // Groq uses OpenAI-compatible Chat Completions format
        Map<String, Object> requestBody = Map.of(
                "model", groqModel,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "You are a resume parser. You always respond with only valid JSON. No explanation, no markdown, no text before or after the JSON object."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.1,
                "max_tokens", 1000,
                "response_format", Map.of("type", "json_object")
        );

        String responseBody = buildClient().post()
                .uri(groqBaseUrl + "/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + groqApiKey)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return parseResponse(responseBody);
    }

    private String buildPrompt(String rawText, List<String> regexSkills) {
        String trimmedResume = rawText.substring(0, Math.min(rawText.length(), 1500));

        return """
            Analyze the resume below and return ONLY a JSON object.
            
            Already found skills by regex: %s
            
            Return exactly this JSON structure:
            {
              "skills": ["skill1", "skill2"],
              "experience": ["Role at Company (year-year)", "Role at Company (year-year)"],
              "summary": "First sentence about candidate. Second sentence about their strengths."
            }
            
            Rules:
            - skills: combine the regex skills above with any additional skills you find in the resume
            - experience: each entry is one string formatted as "Job Title at Company (start-end)"
            - summary: exactly 2 sentences, professional tone
            
            Resume:
            %s
            """.formatted(regexSkills.toString(), trimmedResume);
    }

    private AiResumeResult parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        // Groq follows OpenAI format: choices[0].message.content
        String content = root
                .path("choices").get(0)
                .path("message")
                .path("content")
                .asText();

        // Strip markdown fences just in case
        content = content.replaceAll("```json|```", "").trim();

        // Extract JSON object safely
        int jsonStart = content.indexOf("{");
        int jsonEnd = content.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd != -1) {
            content = content.substring(jsonStart, jsonEnd + 1);
        }

        JsonNode parsed = objectMapper.readTree(content);

        List<String> skills = objectMapper.convertValue(
                parsed.path("skills"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );

        List<String> experience = objectMapper.convertValue(
                parsed.path("experience"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );

        String summary = parsed.path("summary").asText();
        return new AiResumeResult(skills, experience, summary);
    }

    public record AiResumeResult(List<String> skills, List<String> experience, String summary) {}
}