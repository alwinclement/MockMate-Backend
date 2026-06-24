package com.example.MockMate.Service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;

@Service
public class ResumeParserService {

    // Common tech skills to scan for
    private static final List<String> KNOWN_SKILLS = List.of(
            "Java", "Spring Boot", "Spring Security", "Hibernate", "JPA",
            "Python", "Django", "Flask", "FastAPI",
            "JavaScript", "TypeScript", "React", "Angular", "Vue",
            "Node.js", "Express",
            "SQL", "PostgreSQL", "MySQL", "MongoDB", "Redis",
            "Docker", "Kubernetes", "AWS", "GCP", "Azure",
            "Git", "REST", "GraphQL", "Kafka", "RabbitMQ",
            "HTML", "CSS", "Tailwind", "Bootstrap",
            "C", "C++", "C#", ".NET", "Go", "Rust", "Kotlin",
            "Machine Learning", "Deep Learning", "TensorFlow", "PyTorch",
            "Linux", "CI/CD", "Jenkins", "GitHub Actions"
    );

    public String extractName(String text) {
        // First non-empty line is usually the candidate's name
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() < 50
                    && !trimmed.contains("@") && !trimmed.matches(".*\\d{5,}.*")) {
                return trimmed;
            }
        }
        return "Unknown";
    }

    public String extractEmail(String text) {
        Pattern pattern = Pattern.compile(
                "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}"
        );
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    public String extractPhone(String text) {
        Pattern pattern = Pattern.compile(
                "(\\+?\\d[\\d\\s\\-().]{7,}\\d)"
        );
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group().trim() : null;
    }

    public List<String> extractSkills(String text) {
        List<String> found = new ArrayList<>();
        String lowerText = text.toLowerCase();
        for (String skill : KNOWN_SKILLS) {
            if (lowerText.contains(skill.toLowerCase())) {
                found.add(skill);
            }
        }
        return found;
    }

    public List<String> extractExperience(String text) {
        List<String> experiences = new ArrayList<>();
        // Match lines that look like job titles or company entries
        Pattern pattern = Pattern.compile(
                "(?i)(\\d{4}\\s*[-–]\\s*(\\d{4}|present|current)).*",
                Pattern.MULTILINE
        );
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String line = matcher.group().trim();
            if (line.length() > 5 && line.length() < 200) {
                experiences.add(line);
            }
        }
        return experiences;
    }
}
