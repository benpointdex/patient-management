package com.pm.auth_service.service;

import com.pm.auth_service.dto.LoginRequestDTO;
import com.pm.auth_service.dto.RegisterRequestDTO;
import com.pm.auth_service.model.User;
import com.pm.auth_service.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public String register(RegisterRequestDTO registerRequestDTO) {
        if (userService.findByEmail(registerRequestDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(registerRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setRole(registerRequestDTO.getRole());
        userService.save(user);

        return "User registered successfully";
    }

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {

        Optional<String> token= userService.findByEmail(loginRequestDTO.getEmail())
                .filter(user -> passwordEncoder.matches(loginRequestDTO.getPassword(),user.getPassword()))
                .map(user -> jwtUtil.generateToken(user.getEmail(),user.getRole()));

        return token;
    }

    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e){
            return false;
        }
    }
}
