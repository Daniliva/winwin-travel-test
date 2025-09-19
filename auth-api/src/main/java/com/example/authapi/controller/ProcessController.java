package com.example.authapi.controller;

import com.example.authapi.config.JwtUtil;
import com.example.authapi.dto.ProcessRequestDto;
import com.example.authapi.dto.ProcessResponseDto;
import com.example.authapi.service.ProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ProcessController {

    private final JwtUtil jwtUtil;
    private final ProcessService processService;

    @PostMapping("/process")
    @Operation(
            summary = "Process text with authentication",
            description = "Processes text using data-api, requires a valid JWT token",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Text processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or missing request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized due to missing or invalid token"),
            @ApiResponse(responseCode = "503", description = "Data API unavailable")
    })
    public ResponseEntity<?> process(@Valid @RequestBody ProcessRequestDto request,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.debug("Received process request with Authorization header: {}, request: {}", authHeader, request);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header: {}", authHeader);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header. Expected 'Bearer <token>'");
        }

        String token = authHeader.replace("Bearer ", "").trim();
        if (token.isEmpty()) {
            log.warn("Token is empty after removing 'Bearer ' prefix");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token cannot be empty");
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("Token validation failed: {}", token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }

        try {
            UUID userId = jwtUtil.extractUserId(token);
            log.debug("Processing text for user ID: {}", userId);
            ProcessResponseDto response = processService.processText(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error processing text: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Service unavailable: " + e.getMessage());
        }
    }

    @GetMapping("/hello")
    @Operation(summary = "Return a hello message", description = "Returns a simple hello message for testing controller connectivity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hello message returned successfully")
    })
    public ResponseEntity<String> hello() {
        log.debug("Received request for /api/hello");
        return ResponseEntity.ok("hello");
    }
}