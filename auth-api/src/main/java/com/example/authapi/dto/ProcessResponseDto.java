package com.example.authapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response containing processed text")
public class ProcessResponseDto {
    @Schema(description = "Processed text result", example = "OLLEH")
    private String result;
}