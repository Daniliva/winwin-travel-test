package com.example.authapi.controller;

   import com.example.authapi.service.AuthService;
   import io.swagger.v3.oas.annotations.Operation;
   import io.swagger.v3.oas.annotations.responses.ApiResponse;
   import io.swagger.v3.oas.annotations.responses.ApiResponses;
   import io.swagger.v3.oas.annotations.tags.Tag;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.http.HttpStatus;
   import org.springframework.http.ResponseEntity;
   import org.springframework.web.bind.annotation.*;

   import java.util.Map;

   @RestController
   @RequestMapping("/api/auth")
   @Tag(name = "Authentication", description = "API for user registration and login")
   public class AuthController {

       @Autowired
       private AuthService authService;

       @Operation(summary = "Register a new user", description = "Creates a new user with email and password")
       @ApiResponses({
               @ApiResponse(responseCode = "201", description = "User registered successfully"),
               @ApiResponse(responseCode = "400", description = "Email already exists")
       })
       @PostMapping("/register")
       public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
           try {
               authService.register(body.get("email"), body.get("password"));
               return ResponseEntity.status(HttpStatus.CREATED).build();
           } catch (IllegalArgumentException e) {
               return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
           }
       }

       @Operation(summary = "Login a user", description = "Authenticates a user and returns a JWT token")
       @ApiResponses({
               @ApiResponse(responseCode = "200", description = "Login successful, token returned"),
               @ApiResponse(responseCode = "400", description = "Invalid credentials")
       })
       @PostMapping("/login")
       public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
           try {
               String token = authService.login(body.get("email"), body.get("password"));
               return ResponseEntity.ok(Map.of("token", token));
           } catch (RuntimeException e) {
               return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
           }
       }
   }