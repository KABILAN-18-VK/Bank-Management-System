package com.bank.controller;

import com.bank.dto.LoginRequest;
import com.bank.dto.LoginResponse;
import com.bank.dto.RegisterRequest;
import com.bank.entity.User;
import com.bank.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 1. User Registration Endpoint
    // POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        LoginResponse responseData = authService.register(registerRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Registration successful!");
        response.put("data", responseData);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. User Login Endpoint
    // POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse responseData = authService.login(loginRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Login successful!");
        response.put("data", responseData);

        return ResponseEntity.ok(response);
    }

    // 3. Get Current User Profile Endpoint (Protected by Bearer Token)
    // GET http://localhost:8080/api/auth/me
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = authService.getCurrentUser(currentUsername);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", user);

        return ResponseEntity.ok(response);
    }
}
