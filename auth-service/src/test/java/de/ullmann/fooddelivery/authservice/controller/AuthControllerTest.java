package de.ullmann.fooddelivery.authservice.controller;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.authservice.config.SecurityConfig;
import de.ullmann.fooddelivery.authservice.dto.AddressRequest;
import de.ullmann.fooddelivery.authservice.dto.LoginRequest;
import de.ullmann.fooddelivery.authservice.dto.LoginResponse;
import de.ullmann.fooddelivery.authservice.dto.RegisterCustomerRequest;
import de.ullmann.fooddelivery.authservice.dto.RegisterCustomerResponse;
import de.ullmann.fooddelivery.authservice.dto.ValidateRequest;
import de.ullmann.fooddelivery.authservice.dto.ValidateResponse;
import de.ullmann.fooddelivery.authservice.service.AuthService;
import de.ullmann.fooddelivery.common.security.JwtUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final String EMAIL = "user@example.com";
    private static final AddressRequest ADDRESS = new AddressRequest("Main St", "1", "Berlin", "10115", "Germany");

    @Test
    void registerCustomer_shouldReturn201WithRegisterCustomerResponse() throws Exception {
        RegisterCustomerRequest request = new RegisterCustomerRequest("John", "Doe", EMAIL, "secret123", "+49123", ADDRESS);
        when(authService.registerCustomer(any()))
                .thenReturn(new RegisterCustomerResponse(CUSTOMER_ID, "John", "Doe", EMAIL, "+49123"));

        mockMvc.perform(post("/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void login_shouldReturn200WithLoginResponse() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, "secret123");
        when(authService.login(any())).thenReturn(new LoginResponse("jwt-token", CUSTOMER_ID, EMAIL));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void validate_shouldReturn200WithValidateResponse() throws Exception {
        ValidateRequest request = new ValidateRequest("some.jwt.token");
        when(authService.validate(anyString())).thenReturn(new ValidateResponse(CUSTOMER_ID, EMAIL));

        mockMvc.perform(post("/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }
}
