package com.glr.fintech.fintech_core_api.infrastructure.web.controller;

import com.glr.fintech.fintech_core_api.domain.User;
import com.glr.fintech.fintech_core_api.infrastructure.persistence.UserRepository;
import com.glr.fintech.fintech_core_api.infrastructure.security.JwtTokenProvider;
import com.glr.fintech.fintech_core_api.infrastructure.web.dto.AuthResponse;
import com.glr.fintech.fintech_core_api.infrastructure.web.dto.LoginRequest;
import com.glr.fintech.fintech_core_api.infrastructure.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Validar si el username ya existe
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Error: El nombre de usuario ya está en uso.");
        }

        // Validar si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Error: El correo electrónico ya está registrado.");
        }

        // Crear el nuevo usuario
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ROLE_USER)
                .build();

        userRepository.save(newUser);

        return ResponseEntity.ok("¡Usuario registrado exitosamente!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Autenticar credenciales
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generar el Token JWT
        String jwt = tokenProvider.generateToken(authentication);

        AuthResponse response = AuthResponse.builder()
                .token(jwt)
                .username(request.getUsername())
                .message("¡Inicio de sesión exitoso!")
                .build();

        return ResponseEntity.ok(response);
    }
}