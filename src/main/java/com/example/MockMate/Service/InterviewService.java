package com.example.MockMate.Service;

import com.example.MockMate.DTO.InterviewSessionDto;
import com.example.MockMate.DTO.QuestionDto;
import com.example.MockMate.Model.*;
import com.example.MockMate.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepo sessionRepository;
    private final QuestionRepo questionRepository;
    private final ResumeRepo resumeRepository;
    private final UserDetailsRepo userRepository;
    private final QuestionGenerationService questionGenerationService;
    // ↑ Same field name as before — only the implementation changed inside

    @Transactional
    public InterviewSessionDto startInterview(Long resumeId,
                                              JobRole jobRole,
                                              String userEmail) {
        // ── Step 1: Fetch user and resume ─────────────────────────────────
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ResumeModel resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        // ── Step 2: Create and save session first ─────────────────────────
        // We need session.id before saving questions (FK constraint)
        InterviewSession session = InterviewSession.builder()
                .user(user)
                .resume(resume)
                .jobRole(jobRole)
                .status(InterviewSession.SessionStatus.CREATED)
                .build();
        session = sessionRepository.save(session);

        // ── Step 3: Generate questions via Groq ───────────────────────────
        // Pass resume text so Groq tailors questions to the candidate
        List<QuestionGenerationService.GeneratedQuestion> generated;
        try {
            generated = questionGenerationService.generateQuestions(
                    resume.getRawText(), jobRole
            );
        } catch (Exception e) {
            sessionRepository.delete(session);
            throw new RuntimeException("Failed to generate questions via Groq: " + e.getMessage());
        }

        // ── Step 4: Save each question linked to the session ──────────────
        List<Question> savedQuestions = new ArrayList<>();
        for (int i = 0; i < generated.size(); i++) {
            QuestionGenerationService.GeneratedQuestion gq = generated.get(i);

            Question question = Question.builder()
                    .session(session)
                    .content(gq.content())       // getter from @Data
                    .type(gq.type())
                    .difficulty(gq.difficulty())
                    .hint(gq.hint())
                    .orderIndex(i + 1)              // 1-based order
                    .build();

            savedQuestions.add(questionRepository.save(question));
        }

        // ── Step 5: Update session to IN_PROGRESS ─────────────────────────
        session.setStatus(InterviewSession.SessionStatus.IN_PROGRESS);
        sessionRepository.save(session);

        return toDto(session, savedQuestions);
    }

    public List<InterviewSessionDto> getUserSessions(String userEmail) {
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(s -> {
                    List<Question> questions = questionRepository
                            .findBySessionIdOrderByOrderIndex(s.getId());
                    return toDto(s, questions);
                })
                .toList();
    }

    // ── Converts session + questions into the full DTO ────────────────────
    private InterviewSessionDto toDto(InterviewSession session, List<Question> questions) {
        List<QuestionDto> allDtos = questions.stream()
                .map(this::toQuestionDto)
                .toList();

        return new InterviewSessionDto(
                session.getId(),
                session.getJobRole(),
                session.getJobRole().getDisplayName(), // → goes into jobRoleDisplay
                session.getStatus(),
                allDtos,
                allDtos.stream()
                        .filter(q -> q.type() == QuestionType.HR)
                        .toList(),
                allDtos.stream()
                        .filter(q -> q.type() == QuestionType.TECHNICAL)
                        .toList(),
                allDtos.stream()
                        .filter(q -> q.type() == QuestionType.CODING)
                        .toList(),
                session.getCreatedAt()
        );
    }

    // ── Maps Question entity to your original record DTO ─────────────────
    private QuestionDto toQuestionDto(Question q) {
        return new QuestionDto(
                q.getId(),
                q.getContent(),
                q.getType(),
                q.getDifficulty(),
                q.getOrderIndex(),
                q.getHint()
        );
    }
}