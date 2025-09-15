package com.example.dataapi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransformController {

    @Value("${internal.token}")
    private String internalToken;

    @PostMapping("/transform")
    public ResponseEntity<?> transform(@RequestBody Map<String, String> body,
                                       @RequestHeader(value = "X-Internal-Token", required = false) String token) {
        if (token == null || !token.equals(internalToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String text = body.get("text");
        String result = transformText(text);

        return ResponseEntity.ok(Map.of("result", result));
    }

    private String transformText(String text) {
        return new StringBuilder(text.toUpperCase()).reverse().toString();
    }
}