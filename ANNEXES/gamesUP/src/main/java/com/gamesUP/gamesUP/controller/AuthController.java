package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.dto.auth.LoginRequest;
import com.gamesUP.gamesUP.dto.auth.LoginResponse;
import com.gamesUP.gamesUP.security.CustomUserDetails;
import com.gamesUP.gamesUP.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return LoginResponse.bearer(jwtService.generateToken(userDetails));
    }
}
