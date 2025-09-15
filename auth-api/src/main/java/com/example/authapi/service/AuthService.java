package com.example.authapi.service;

   import com.example.authapi.config.JwtUtil;
   import com.example.authapi.model.User;
   import com.example.authapi.repository.UserRepository;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
   import org.springframework.stereotype.Service;

   @Service
   public class AuthService {
       @Autowired
       private UserRepository userRepository;

       @Autowired
       private JwtUtil jwtUtil;

       private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

       public void register(String email, String password) {
           if (userRepository.findByEmail(email).isPresent()) {
               throw new IllegalArgumentException("Email exists");
           }
           User user = new User();
           user.setEmail(email);
           user.setPasswordHash(encoder.encode(password));
           userRepository.save(user);
       }

       public String login(String email, String password) {
           User user = userRepository.findByEmail(email)
                   .orElseThrow(() -> new RuntimeException("User not found"));
           if (!encoder.matches(password, user.getPasswordHash())) {
               throw new RuntimeException("Invalid credentials");
           }
           return jwtUtil.generateToken(user.getId());
       }
   }