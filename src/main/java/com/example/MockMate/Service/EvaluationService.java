package com.example.MockMate.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.MockMate.Model.Answer;
import com.example.MockMate.Model.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EvaluationService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.base-url}")
    private String groqBaseUrl;

    @Value("${groq.model}")
    private String groqModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record EvaluationResult(
            int confidenceScore,
            int depthScore,
            int communicationScore,
            int gapsScore,
            String overallFeedback,
            List<String> weakAreas,
            List<String> suggestedTopics
    ) {}

    private RestClient buildClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        return RestClient.builder().requestFactory(factory).build();
    }

    public EvaluationResult evaluateSession(List<Question> questions, List<Answer> answers, String jobRoleDisplay) throws Exception {
        String prompt = buildPrompt(questions, answers, jobRoleDisplay);

        Map<String, Object> requestBody = Map.of(
                "model", groqModel,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "You are a senior technical interview coach. You always respond with only valid JSON. No explanation, no markdown, no preamble."
                        ),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.4,
                "max_tokens", 1500,
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

    private String buildPrompt(List<Question> questions, List<Answer> answers, String jobRoleDisplay) {
        StringBuilder transcript = new StringBuilder();

        // Build a Q&A transcript by matching each question to its answer
        for (Question q : questions) {
            Answer matchingAnswer = answers.stream()
                    .filter(a -> a.getQuestion().getId().equals(q.getId()))
                    .findFirst()
                    .orElse(null);

            transcript.append("Q: ").append(q.getContent()).append("\n");

            if (matchingAnswer == null || matchingAnswer.getTimedOut() && matchingAnswer.getAnswerText().isBlank()) {
                transcript.append("A: [No answer given - candidate ran out of time]\n\n");
            } else {
                transcript.append("A: ").append(matchingAnswer.getAnswerText()).append("\n");
                transcript.append("(Answered in ").append(matchingAnswer.getTimeTakenSeconds()).append("s)\n\n");
            }
        }

        return """
            You are evaluating a complete mock interview transcript for a candidate
            applying for the role of: %s
            
            Score the candidate's overall performance across this entire interview
            on these four categories, each on a scale of 1 to 5 stars:
            
            - confidenceScore: how confident and assured the answers sound (tone, decisiveness, hedging)
            - depthScore: how deep and technically substantive the answers are (specifics, examples, reasoning)
            - communicationScore: how clearly and structurally the answers are communicated (organization, clarity)
            - gapsScore: 5 means no knowledge gaps found, 1 means significant gaps in core concepts
            
            Also identify:
            - weakAreas: 2-4 short specific phrases naming what needs improvement (e.g. "System design trade-offs", "STAR method structure")
            - suggestedTopics: 2-4 short specific actionable practice suggestions
            - overallFeedback: exactly 2-3 sentences summarizing performance, encouraging but honest
            
            Base your scores on patterns across the WHOLE interview, not just one answer.
            Unanswered or timed-out questions should lower depthScore and gapsScore.
            
            Full interview transcript:
            %s
            
            Return ONLY this JSON structure:
            {
              "confidenceScore": 4,
              "depthScore": 3,
              "communicationScore": 4,
              "gapsScore": 3,
              "overallFeedback": "...",
              "weakAreas": ["...", "..."],
              "suggestedTopics": ["...", "..."]
            }
            """.formatted(jobRoleDisplay, transcript.toString());
    }

    private EvaluationResult parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        String content = root
                .path("choices").get(0)
                .path("message")
                .path("content")
                .asText();

        content = content.replaceAll("```json|```", "").trim();

        int jsonStart = content.indexOf("{");
        int jsonEnd = content.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd != -1) {
            content = content.substring(jsonStart, jsonEnd + 1);
        }

        JsonNode parsed = objectMapper.readTree(content);

        int confidence = clamp(parsed.path("confidenceScore").asInt(3));
        int depth = clamp(parsed.path("depthScore").asInt(3));
        int communication = clamp(parsed.path("communicationScore").asInt(3));
        int gaps = clamp(parsed.path("gapsScore").asInt(3));

        List<String> weakAreas = objectMapper.convertValue(
                parsed.path("weakAreas"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );

        List<String> suggestedTopics = objectMapper.convertValue(
                parsed.path("suggestedTopics"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );

        return new EvaluationResult(
                confidence, depth, communication, gaps,
                parsed.path("overallFeedback").asText(),
                weakAreas,
                suggestedTopics
        );
    }

    // Defensive clamp — guarantees the score is always 1-5 even if the model
    // returns something unexpected like 0, 7, or a decimal
    private int clamp(int value) {
        if (value < 1) return 1;
        if (value > 5) return 5;
        return value;
    }
}