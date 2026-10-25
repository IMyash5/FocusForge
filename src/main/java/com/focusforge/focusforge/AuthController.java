
package com.focusforge.focusforge;

import com.focusforge.focusforge.dto.AuthResponse;
import com.focusforge.focusforge.dto.LoginRequest;
import com.focusforge.focusforge.dto.LoginResponse;
import com.focusforge.focusforge.dto.RegisterRequest;
import com.focusforge.focusforge.model.AppUser;
import com.focusforge.focusforge.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AppUser user = authService.register(request);

        AuthResponse response = new AuthResponse(
                user.getId(),
                user.getEmail(),
                "Registration successful");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AppUser user = authService.validateLogin(request);

        String token = authService.generateAccessToken(user);

        LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                1800);

        return ResponseEntity.ok(response);
    }
}
