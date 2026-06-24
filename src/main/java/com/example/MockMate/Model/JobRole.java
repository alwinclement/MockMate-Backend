package com.example.MockMate.Model;

import lombok.Getter;

@Getter
public enum JobRole {
    BACKEND_ENGINEER("Backend Engineer"),
    FRONTEND_ENGINEER("Frontend Engineer"),
    FULLSTACK_ENGINEER("Full Stack Engineer"),
    DATA_SCIENTIST("Data Scientist"),
    DEVOPS_ENGINEER("DevOps Engineer"),
    MOBILE_ENGINEER("Mobile Engineer"),
    ML_ENGINEER("Machine Learning Engineer"),
    QA_ENGINEER("QA Engineer"),
    SYSTEM_DESIGN("System Design");

    private final String displayName;

    JobRole(String displayName) {
        this.displayName = displayName;
    }

}