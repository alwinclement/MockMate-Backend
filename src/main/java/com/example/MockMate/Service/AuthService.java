package com.example.MockMate.Service;

import com.example.MockMate.DTO.AuthResponse;
import com.example.MockMate.DTO.LoginRequest;
import com.example.MockMate.DTO.RegisterRequest;
import com.example.MockMate.Model.Role;
import com.example.MockMate.Model.UserModel;
import com.example.MockMate.Repository.UserDetailsRepo;
import com.example.MockMate.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDetailsRepo userDetailsRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {

        // ── Step 1: Check if email already exists ──────────────────────────
        if (userDetailsRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");

        }

        // ── Step 2: Build user — backend hardcodes role as "USER" ──────────
        // @Data gives us setters but no builder, so we use constructor
        // UserModel still has @Builder so we use that
        UserModel user = UserModel.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // never store plain text
                .role(Role.USER) // backend decides — client never touches this
                .build();

        // ── Step 3: Save to database ────────────────────────────────────────
        userDetailsRepo.save(user);

        // ── Step 4: Generate JWT and return only the token ─────────────────
        // AuthResponse only has token field now, matching your @Data DTO
        String token = jwtService.generateToken(
                toUserDetails(user),
                Map.of("role", user.getRole()) // role embedded inside token payload
        );

        // AuthResponse has no @Builder, uses @AllArgsConstructor
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .name(user.getName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        // ── Step 1: Authenticate credentials ───────────────────────────────
        // AuthenticationManager checks email + password against DB
        // Automatically throws BadCredentialsException if wrong — no manual check needed
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // ── Step 2: Fetch the saved user from DB ────────────────────────────
        UserModel user = userDetailsRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(
                toUserDetails(user),
                Map.of("role", user.getRole())
        );

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // ── Helper: converts your UserModel into Spring's UserDetails ──────────
    private UserDetails toUserDetails(UserModel user) {
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name()) // "USER" becomes "ROLE_USER"
                .build();
    }
}