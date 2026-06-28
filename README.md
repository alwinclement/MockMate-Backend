# MockMate — Backend

AI-powered mock interview platform backend built with Spring Boot. Generates personalized interview questions from a candidate's resume, runs live timed interview sessions over WebSocket, and evaluates performance using AI.

## Live Demo
Frontend: https://mock-mate-frontend-gilt.vercel.app
Backend API: https://mockmate-backend-m40e.onrender.com

## Tech Stack
- Java 21, Spring Boot, Spring Security, JWT
- PostgreSQL (Neon), Hibernate/JPA
- WebSocket (STOMP over SockJS)
- Apache PDFBox for resume parsing
- Groq API (Llama 3.3 70B) for AI question generation and evaluation
- Deployed on Render

## Features
- JWT-based authentication with role-based access control (USER/ADMIN)
- Resume upload and parsing (PDFBox + regex + AI enrichment)
- AI-generated interview questions categorized by type (HR/Technical/Coding) and difficulty
- Real-time live interview engine with server-side countdown timers
- AI evaluation scoring across confidence, depth, communication, and knowledge gaps
- Admin panel with user management and platform analytics

## Architecture
[Auth] → [Resume Upload + AI Parsing] → [Question Generation] → [Live WebSocket Interview] → [AI Evaluation] → [Dashboard]

## Running locally
\`\`\`bash
git clone https://github.com/yourusername/MockMate.git
cd MockMate
# Set environment variables: DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD, JWT_SECRET, GROQ_API_KEY
./mvnw spring-boot:run
\`\`\`
