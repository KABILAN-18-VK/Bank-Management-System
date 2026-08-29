package com.bank.controller;

import com.bank.banking_management_system.BankingManagementSystemApplication;
import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BankingManagementSystemApplication.class)
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setFullName("Alice Smith");
        validRegisterRequest.setUsername("alicesmith");
        validRegisterRequest.setEmail("alice@example.com");
        validRegisterRequest.setPassword("Secret123!");
        validRegisterRequest.setRole("ROLE_USER");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsernameOrEmail("alicesmith");
        validLoginRequest.setPassword("Secret123!");
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterUser_Success() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Registration successful")))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.username", is("alicesmith")))
                .andExpect(jsonPath("$.data.email", is("alice@example.com")));
    }

    @Test
    @DisplayName("Should reject registration if username already exists")
    void testRegisterUser_DuplicateUsername() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated());

        RegisterRequest duplicateReq = new RegisterRequest();
        duplicateReq.setFullName("Alice Duplicate");
        duplicateReq.setUsername("alicesmith");
        duplicateReq.setEmail("different@example.com");
        duplicateReq.setPassword("Secret123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Username is already taken")));
    }

    @Test
    @DisplayName("Should reject registration with invalid email format")
    void testRegisterUser_InvalidEmail() throws Exception {
        RegisterRequest invalidEmailReq = new RegisterRequest();
        invalidEmailReq.setFullName("Bob BadEmail");
        invalidEmailReq.setUsername("bobbademail");
        invalidEmailReq.setEmail("not-an-email");
        invalidEmailReq.setPassword("Secret123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors.email", notNullValue()));
    }

    @Test
    @DisplayName("Should successfully login registered user")
    void testLoginUser_Success() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.username", is("alicesmith")));
    }

    @Test
    @DisplayName("Should reject login with wrong password")
    void testLoginUser_WrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)));

        LoginRequest wrongPassReq = new LoginRequest("alicesmith", "WrongPassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPassReq)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should fetch user profile using Bearer JWT token")
    void testGetCurrentUser_Success() throws Exception {
        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = regResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(jsonResponse).path("data").path("token").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("alicesmith")))
                .andExpect(jsonPath("$.data.email", is("alice@example.com")))
                .andExpect(jsonPath("$.data.fullName", is("Alice Smith")));
    }
}
