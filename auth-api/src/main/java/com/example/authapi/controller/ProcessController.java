package com.example.authapi.controller;

import com.example.authapi.config.JwtUtil;
import com.example.authapi.model.ProcessingLog;
import com.example.authapi.repository.ProcessingLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Text Processing", description = "API for processing text with data-api")
public class ProcessController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProcessingLogRepository logRepository;

    @Value("${internal.token}")
    private String internalToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @Operation(summary = "Process text", description = "Sends text to data-api for transformation and logs the result")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Text processed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "503", description = "data-api unavailable")
    })
    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = jwtUtil.extractUserId(token);
        String text = body.get("text");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.set("X-Internal-Token", internalToken);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("text", text), headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity("http://data-api:8081/api/transform", request, Map.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return ResponseEntity.status(response.getStatusCode()).build();
            }

            String result = (String) response.getBody().get("result");
            if (result == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            ProcessingLog log = new ProcessingLog();
            log.setUserId(userId);
            log.setInputText(text);
            log.setOutputText(result);
            log.setCreatedAt(LocalDateTime.now());
            logRepository.save(log);

            return ResponseEntity.ok(Map.of("result", result));
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}