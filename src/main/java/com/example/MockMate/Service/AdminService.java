package com.example.MockMate.Service;

import com.example.MockMate.DTO.AdminStatsDto;
import com.example.MockMate.DTO.AdminUserDto;
import com.example.MockMate.Model.Role;
import com.example.MockMate.Model.UserModel;
import com.example.MockMate.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserDetailsRepo userDetailsRepo;
    private final ResumeRepo resumeRepo;
    private final InterviewSessionRepo interviewSessionRepo;
    private final ReportRepo reportRepository;

    public List<AdminUserDto> getAllUsers() {
        List<UserModel> users = userDetailsRepo.findAll();

        return users.stream().map(u -> AdminUserDto.builder()
                .id(u.getId())
                .email(u.getEmail())
                .role(u.getRole())
                .resumeCount(resumeRepo.findByUserId(u.getId()).size())
                .sessionCount(interviewSessionRepo.findByUserIdOrderByCreatedAtDesc(u.getId()).size())
                .reportCount((int) reportRepository.findByUserIdOrderByGeneratedAtAsc(u.getId()).size())
                .build()
        ).toList();
    }

    public AdminUserDto updateUserRole(Long userId, Role newRole) {
        UserModel user = userDetailsRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(newRole);
        userDetailsRepo.save(user);

        return AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .resumeCount(resumeRepo.findByUserId(user.getId()).size())
                .sessionCount(interviewSessionRepo.findByUserIdOrderByCreatedAtDesc(user.getId()).size())
                .reportCount((int) reportRepository.findByUserIdOrderByGeneratedAtAsc(user.getId()).size())
                .build();
    }

    public void deleteUser(Long userId) {
        if (!userDetailsRepo.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userDetailsRepo.deleteById(userId);
        // Cascades to resumes/sessions/reports only if your entity relationships
        // are set to CascadeType.ALL — otherwise this throws a foreign key error,
        // which is actually a safer default for an admin delete action.
    }

    public AdminStatsDto getStats() {
        long totalUsers = userDetailsRepo.count();
        long totalResumes = resumeRepo.count();
        long totalSessions = interviewSessionRepo.count();
        List<com.example.MockMate.Model.Report> allReports = reportRepository.findAll();
        long totalReports = allReports.size();

        double avgScore = allReports.isEmpty()
                ? 0.0
                : allReports.stream().mapToDouble(com.example.MockMate.Model.Report::getOverallScore).average().orElse(0.0);

        return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .totalResumes(totalResumes)
                .totalSessions(totalSessions)
                .totalReports(totalReports)
                .averageOverallScore(Math.round(avgScore * 10) / 10.0)
                .build();
    }
}