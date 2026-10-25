
package com.focusforge.focusforge.service;

import com.focusforge.focusforge.dto.LoginRequest;
import com.focusforge.focusforge.dto.RegisterRequest;
import com.focusforge.focusforge.model.AppUser;
import com.focusforge.focusforge.repository.AppUserRepository;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        String password = request.getPassword();

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Password must not exceed 72 UTF-8 bytes.");
        }

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists.");
        }

        AppUser user = new AppUser(
                email,
                passwordEncoder.encode(password));

        return appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser validateLogin(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        AppUser user = appUserRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException(
                        "Invalid email or password."));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new BadCredentialsException(
                    "Invalid email or password.");
        }

        return user;
    }

    public String generateAccessToken(AppUser user) {

        return jwtService.generateToken(
                user.getId(),
                user.getEmail());
    }
}
