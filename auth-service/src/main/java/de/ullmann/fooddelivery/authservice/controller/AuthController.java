package de.ullmann.fooddelivery.authservice.controller;

import de.ullmann.fooddelivery.authservice.dto.AuthResponse;
import de.ullmann.fooddelivery.authservice.dto.LoginRequest;
import de.ullmann.fooddelivery.authservice.dto.RegisterCustomerRequest;
import de.ullmann.fooddelivery.authservice.dto.RegisterStaffRequest;
import de.ullmann.fooddelivery.authservice.dto.StaffResponse;
import de.ullmann.fooddelivery.authservice.dto.ValidateRequest;
import de.ullmann.fooddelivery.authservice.dto.ValidateResponse;
import de.ullmann.fooddelivery.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/customer")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerCustomer(@RequestBody @Valid RegisterCustomerRequest req) {
        return authService.registerCustomer(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/register/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public StaffResponse registerStaff(@RequestBody @Valid RegisterStaffRequest req) {
        return authService.registerStaff(req);
    }

    @PostMapping("/validate")
    public ValidateResponse validate(@RequestBody @Valid ValidateRequest req) {
        return authService.validate(req.token());
    }
}
