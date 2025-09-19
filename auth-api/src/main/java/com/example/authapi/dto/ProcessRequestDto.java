package com.example.authapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProcessRequestDto {
    @NotBlank(message = "Text is required")
    private String text;
}