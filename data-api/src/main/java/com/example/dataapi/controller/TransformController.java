package com.example.dataapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@Slf4j
public class TransformController {

    @Value("${internal.token:defaultToken}")
    private String internalToken;

    @PostMapping(value = "/transform", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Transform text", description = "Transforms input text by converting to uppercase and reversing it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Text transformed successfully"),
            @ApiResponse(responseCode = "400", description = "Missing 'text' field in request body"),
            @ApiResponse(responseCode = "403", description = "Invalid or missing internal token")
    })
    public ResponseEntity<?> transform(@RequestBody Map<String, String> body,
                                       @RequestHeader(value = "X-Internal-Token", required = false) String token) {
        log.debug("Received transform request with token: {}, expected internalToken: {}", token, internalToken);
        if (token == null || !token.equals(internalToken)) {
            log.warn("Invalid or missing token. Received: {}, Expected: {}", token, internalToken);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid or missing internal token"));
        }

        String text = body.get("text");
        if (text == null) {
            log.warn("Missing 'text' field in request body");
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'text' field"));
        }

        String result = transformText(text);
        log.info("Text transformed successfully: {} -> {}", text, result);
        return ResponseEntity.ok(Map.of("result", result));
    }

    private String transformText(String text) {
        return new StringBuilder(text.toUpperCase()).reverse().toString();
    }
}