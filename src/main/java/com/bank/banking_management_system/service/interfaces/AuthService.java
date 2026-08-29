package com.bank.service;

import com.bank.dto.LoginRequest;
import com.bank.dto.LoginResponse;
import com.bank.dto.RegisterRequest;
import com.bank.entity.User;

public interface AuthService {
    LoginResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
    User getCurrentUser(String username);
}
