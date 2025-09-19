package com.example.authapi.dto;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String token;

    public  AuthResponseDto(String token){
        this.token=token;
    }
}