package com.example.MockMate.Service;

import com.example.MockMate.DTO.AnswerDto;
import com.example.MockMate.DTO.InterviewStateDto;
import com.example.MockMate.DTO.SubmitAnswerRequest;
import com.example.MockMate.Model.*;
import com.example.MockMate.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LiveInterviewService {

    // Per-question time limit in seconds
    private static final int QUESTION_TIME_SECONDS = 120;

    private final SimpMessagingTemplate messagingTemplate;
    private final InterviewSessionRepo sessionRepository;
    private final QuestionRepo questionRepository;
    private final AnswerRepo answerRepository;
    private final ReportService reportService;

    // In-memory state for active sessions
    // Key: sessionId, Value: current state
    private final Map<Long, ActiveSessionState> activeSessions = new ConcurrentHashMap<>();

    // Holds the live state of one active interview session
    private static class ActiveSessionState {
        Long sessionId;
        List<Question> questions;
        int currentIndex = 0;
        int timeRemaining = QUESTION_TIME_SECONDS;
        boolean running = true;

        ActiveSessionState(Long sessionId, List<Question> questions) {
            this.sessionId = sessionId;
            this.questions = questions;
        }

        Question currentQuestion() {
            return questions.get(currentIndex);
        }

        boolean hasNext() {
            return currentIndex < questions.size() - 1;
        }
    }

    // Called when user starts the live interview
    public void startLiveSession(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() == InterviewSession.SessionStatus.COMPLETED) {
            throw new RuntimeException("This interview session has already been completed");
        }

        List<Question> questions = questionRepository
                .findBySessionIdOrderByOrderIndex(sessionId);

        if (questions.isEmpty()) {
            throw new RuntimeException("No questions found for session " + sessionId);
        }

        ActiveSessionState state = new ActiveSessionState(sessionId, questions);
        activeSessions.put(sessionId, state);

        pushCurrentQuestion(state);
    }

    // Called every second by the scheduler — ticks the timer
    @Scheduled(fixedDelay = 1000)
    public void tickTimers() {
        for (ActiveSessionState state : activeSessions.values()) {
            if (!state.running) continue;

            state.timeRemaining--;

            if (state.timeRemaining <= 0) {
                // Time ran out — save a timed-out answer and move on
                handleTimedOut(state);
            }
        }
    }

    // User submitted an answer before time ran out
    public AnswerDto submitAnswer(SubmitAnswerRequest request) {
        ActiveSessionState state = activeSessions.get(request.getSessionId());
        if (state == null) {
            throw new RuntimeException("No active session found for id " + request.getSessionId());
        }

        // Save the answer
        AnswerDto saved = saveAnswer(
                request.getSessionId(),
                request.getQuestionId(),
                request.getAnswerText(),
                request.isAnsweredByVoice(),
                request.getTimeTakenSeconds(),
                false
        );

        // Move to next question or end session
        advanceSession(state);
        return saved;
    }

    private void handleTimedOut(ActiveSessionState state) {
        // Save empty timed-out answer for current question
        saveAnswer(
                state.sessionId,
                state.currentQuestion().getId(),
                "",
                false,
                QUESTION_TIME_SECONDS,
                true
        );

        advanceSession(state);
    }

    private void advanceSession(ActiveSessionState state) {
        if (state.hasNext()) {
            state.currentIndex++;
            state.timeRemaining = QUESTION_TIME_SECONDS;
            pushCurrentQuestion(state);
        } else {
            completeSession(state);
        }
    }

    private void pushCurrentQuestion(ActiveSessionState state) {
        Question q = state.currentQuestion();

        InterviewStateDto dto = InterviewStateDto.builder()
                .sessionId(state.sessionId)
                .event("QUESTION")
                .questionId(q.getId())
                .questionContent(q.getContent())
                .questionType(q.getType())
                .difficulty(q.getDifficulty())
                .hint(q.getHint())
                .currentIndex(state.currentIndex)
                .totalQuestions(state.questions.size())
                .timeRemainingSeconds(state.timeRemaining)
                .build();

        // Push to all clients subscribed to this session's topic
        messagingTemplate.convertAndSend(
                "/topic/session/" + state.sessionId, dto
        );
    }

    private void completeSession(ActiveSessionState state) {
        state.running = false;
        activeSessions.remove(state.sessionId);

        // Mark session as completed in DB
        sessionRepository.findById(state.sessionId).ifPresent(session -> {
            session.setStatus(InterviewSession.SessionStatus.COMPLETED);
            sessionRepository.save(session);
        });
        //— trigger AI evaluation now that all answers are saved
        reportService.generateReportForSession(state.sessionId);

        long totalAnswered = answerRepository.countBySessionId(state.sessionId);
        long timedOut = answerRepository.findBySessionId(state.sessionId)
                .stream().filter(a -> Boolean.TRUE.equals(a.getTimedOut())).count();

        InterviewStateDto dto = InterviewStateDto.builder()
                .sessionId(state.sessionId)
                .event("COMPLETED")
                .totalAnswered((int) totalAnswered)
                .timedOutCount((int) timedOut)
                .totalQuestions(state.questions.size())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/session/" + state.sessionId, dto
        );
    }

    private AnswerDto saveAnswer(Long sessionId, Long questionId,
                                 String text, boolean byVoice,
                                 int timeTaken, boolean timedOut) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Answer answer = Answer.builder()
                .question(question)
                .session(session)
                .answerText(text)
                .answeredByVoice(byVoice)
                .timeTakenSeconds(timeTaken)
                .timedOut(timedOut)
                .build();

        Answer saved = answerRepository.save(answer);

        return AnswerDto.builder()
                .id(saved.getId())
                .questionId(questionId)
                .questionContent(question.getContent())
                .answerText(saved.getAnswerText())
                .answeredByVoice(saved.getAnsweredByVoice())
                .timeTakenSeconds(saved.getTimeTakenSeconds())
                .timedOut(saved.getTimedOut())
                .answeredAt(saved.getAnsweredAt())
                .build();
    }

    public List<AnswerDto> getSessionAnswers(Long sessionId) {
        return answerRepository.findBySessionId(sessionId)
                .stream()
                .map(a -> AnswerDto.builder()
                        .id(a.getId())
                        .questionId(a.getQuestion().getId())
                        .questionContent(a.getQuestion().getContent())
                        .answerText(a.getAnswerText())
                        .answeredByVoice(a.getAnsweredByVoice())
                        .timeTakenSeconds(a.getTimeTakenSeconds())
                        .timedOut(a.getTimedOut())
                        .answeredAt(a.getAnsweredAt())
                        .build())
                .toList();
    }
}