package com.example.MockMate.DTO;

import com.example.MockMate.Model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {
    private Long id;
    private String email;
    private Role role;
    private int resumeCount;
    private int sessionCount;
    private int reportCount;
}