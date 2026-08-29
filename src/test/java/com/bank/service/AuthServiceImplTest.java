package com.bank.service;

import com.bank.dto.LoginRequest;
import com.bank.dto.LoginResponse;
import com.bank.dto.RegisterRequest;
import com.bank.entity.User;
import com.bank.exception.BadRequestException;
import com.bank.repository.UserRepository;
import com.bank.security.JwtUtil;
import com.bank.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("john_doe", "john@example.com", "Password123!", "John Doe", "ROLE_USER");
        loginRequest = new LoginRequest("john_doe", "Password123!");
        mockUser = new User("john_doe", "john@example.com", "encodedPassword", "John Doe", "ROLE_USER");
        mockUser.setId(1L);
    }

    @Test
    @DisplayName("Unit Test: Successful User Registration")
    void testRegister_Success() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtil.generateToken(authentication)).thenReturn("mock-jwt-token");

        LoginResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("john_doe", response.getUsername());
        assertEquals("john@example.com", response.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Unit Test: Reject Registration when Username Exists")
    void testRegister_DuplicateUsername_ThrowsException() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.register(registerRequest));

        assertTrue(exception.getMessage().contains("Username is already taken"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Unit Test: Successful User Login")
    void testLogin_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtil.generateToken(authentication)).thenReturn("mock-jwt-token");
        when(userRepository.findByUsernameOrEmail("john_doe", "john_doe")).thenReturn(Optional.of(mockUser));

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("john_doe", response.getUsername());
        assertEquals("Login successful!", response.getMessage());
    }
}
