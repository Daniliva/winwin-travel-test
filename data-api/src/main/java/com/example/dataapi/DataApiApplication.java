package com.example.dataapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableWebMvc
public class DataApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataApiApplication.class, args);
    }
}