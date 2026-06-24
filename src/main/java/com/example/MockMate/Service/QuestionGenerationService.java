package com.example.MockMate.Service;

import com.example.MockMate.Model.JobRole;
import com.example.MockMate.Model.Question;
import com.example.MockMate.Model.QuestionType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class QuestionGenerationService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.base-url}")
    private String groqBaseUrl;

    @Value("${groq.model}")
    private String groqModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // GeneratedQuestion uses your existing GroqQuestionResponse fields
    public record GeneratedQuestion(
            String content,
            QuestionType type,
            Question.Difficulty difficulty,
            String hint
    ) {}

    private RestClient buildClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        return RestClient.builder().requestFactory(factory).build();
    }

    public List<GeneratedQuestion> generateQuestions(String resumeText, JobRole role) throws Exception {
        String prompt = buildPrompt(resumeText, role);

        Map<String, Object> requestBody = Map.of(
                "model", groqModel,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "You are a senior technical interviewer. You always respond with only valid JSON. No explanation, no markdown, no preamble. Only the JSON object."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.7,
                "max_tokens", 3000,
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

    private String buildPrompt(String resumeText, JobRole role) {
        String trimmedResume = resumeText.substring(0, Math.min(resumeText.length(), 2000));

        return """
            You are interviewing a candidate for the role of: %s
            
            Study their resume carefully and generate exactly 10 interview questions.
            Questions must be specific to their actual skills, projects, and technologies.
            Do NOT ask generic questions — reference what is in their resume directly.
            
            Strict distribution:
            - Exactly 3 questions with type "HR"
            - Exactly 4 questions with type "TECHNICAL"
            - Exactly 3 questions with type "CODING"
            
            Strict difficulty rules:
            - HR questions: difficulty must be "EASY" or "MEDIUM" only
            - TECHNICAL questions: difficulty must be "EASY" or "MEDIUM" only
            - CODING questions: difficulty must be "EASY" or "MEDIUM" only
            
            Valid type values: HR, TECHNICAL, CODING
            Valid difficulty values: EASY, MEDIUM
            
            Candidate resume:
            %s
            
            Return ONLY this JSON structure with all 10 questions filled in:
            {
              "questions": [
                {
                  "content": "full question text here",
                  "type": "HR",
                  "difficulty": "EASY",
                  "hint": "key points a strong answer should cover"
                }
              ]
            }
            """.formatted(role.getDisplayName(), trimmedResume);
    }

    private List<GeneratedQuestion> parseResponse(String responseBody) throws Exception {
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
        JsonNode questionsNode = parsed.path("questions");

        List<GeneratedQuestion> result = new ArrayList<>();
        for (JsonNode q : questionsNode) {
            String type = q.path("type").asText("TECHNICAL");
            String difficulty = q.path("difficulty").asText("MEDIUM");

            QuestionType questionType;
            try {
                questionType = QuestionType.valueOf(type.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                questionType = QuestionType.TECHNICAL;
            }

            Question.Difficulty diff;
            try {
                diff = Question.Difficulty.valueOf(difficulty.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                diff = Question.Difficulty.MEDIUM;
            }

            result.add(new GeneratedQuestion(
                    q.path("content").asText(),
                    questionType,
                    diff,
                    q.path("hint").asText(null)
            ));
        }

        return result;
    }
}