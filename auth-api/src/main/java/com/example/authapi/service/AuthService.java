package com.example.authapi.service;

import com.example.authapi.config.JwtUtil;
import com.example.authapi.dto.AuthRequestDto;
import com.example.authapi.dto.AuthResponseDto;
import com.example.authapi.model.User;
import com.example.authapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponseDto register(AuthRequestDto request) {
        log.debug("Registration attempt for email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            throw new IllegalArgumentException("Email exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        log.info("User registered successfully: {}", request.getEmail());
        return new AuthResponseDto(jwtUtil.generateToken(user.getId()));
    }

    public AuthResponseDto login(AuthRequestDto request) {
        log.debug("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User {} not found", request.getEmail());
                    return new RuntimeException("User not found");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Invalid credentials for email {}", request.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        log.info("User logged in successfully: {}", request.getEmail());
        return new AuthResponseDto(jwtUtil.generateToken(user.getId()));
    }
}