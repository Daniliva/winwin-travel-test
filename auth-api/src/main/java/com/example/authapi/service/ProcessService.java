package com.example.authapi.service;

import com.example.authapi.dto.ProcessRequestDto;
import com.example.authapi.dto.ProcessResponseDto;
import com.example.authapi.model.ProcessingLog;
import com.example.authapi.repository.ProcessingLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProcessService {
    private final ProcessingLogRepository logRepository;
    private final RestTemplate restTemplate;
    private final String dataApiUrl = "http://data-api:8081/api/transform";
    @Value("${internal.token:defaultToken}")
    private String internalToken;
    private static final int MAX_TEXT_LENGTH = 1000;

    @Operation(summary = "Process text", description = "Sends text to data-api for transformation and logs the result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Text processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input text"),
            @ApiResponse(responseCode = "503", description = "Data API unavailable")
    })
    public ProcessResponseDto processText(UUID userId, @Valid ProcessRequestDto request) {
        if (userId == null) {
            log.warn("User ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        String text = request.getText();
        if (text == null) {
            log.warn("Text is null in request");
            throw new IllegalArgumentException("Text cannot be null");
        }

        if (text.length() > MAX_TEXT_LENGTH) {
            log.warn("Text too long for user {}: length {}", userId, text.length());
            throw new IllegalArgumentException("Text exceeds maximum length of " + MAX_TEXT_LENGTH + " characters");
        }

        log.debug("Processing text for user {}: {}", userId, text);
        log.debug("Sending request to data-api with X-Internal-Token: {}", internalToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Token", internalToken);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("text", text), headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(dataApiUrl, entity, Map.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.warn("Failed to process text from data-api: status {}", response.getStatusCode());
                throw new RuntimeException("Data API unavailable, status: " + response.getStatusCode());
            }

            String result = (String) response.getBody().get("result");
            if (result == null) {
                log.warn("Invalid response from data-api: no 'result' field");
                throw new RuntimeException("Invalid response from data-api: missing 'result' field");
            }

            ProcessingLog processingLog = new ProcessingLog();
            processingLog.setUserId(userId);
            processingLog.setInputText(text);
            processingLog.setOutputText(result);
            processingLog.setCreatedAt(LocalDateTime.now());
            logRepository.save(processingLog);

            log.info("Text processed successfully for user {}: {} -> {}", userId, text, result);
            ProcessResponseDto responseDto = new ProcessResponseDto();
            responseDto.setResult(result);
            return responseDto;

        } catch (RestClientException e) {
            log.error("Error calling data-api for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Service unavailable: " + e.getMessage(), e);
        }
    }
}