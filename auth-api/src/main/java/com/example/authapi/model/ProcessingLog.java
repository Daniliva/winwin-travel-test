package com.example.authapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processing_log")
@Data
@Schema(description = "Log entry for text processing requests")
public class ProcessingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    @Schema(description = "Unique identifier of the log entry", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "ID of the user who initiated the request", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Column(name = "input_text", nullable = false, length = 1000)
    @Schema(description = "Input text sent for processing", example = "Hello, World!")
    private String inputText;

    @Column(name = "output_text", nullable = false, length = 1000)
    @Schema(description = "Processed output text", example = "DLROW ,OLLEH")
    private String outputText;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Timestamp when the log was created", example = "2025-09-18T15:30:00")
    private LocalDateTime createdAt = LocalDateTime.now();
}