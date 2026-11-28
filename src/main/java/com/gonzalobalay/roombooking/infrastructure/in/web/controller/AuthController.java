package com.gonzalobalay.roombooking.infrastructure.in.web.controller;

import com.gonzalobalay.roombooking.infrastructure.in.web.dto.TokenRequest;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.TokenResponse;
import com.gonzalobalay.roombooking.infrastructure.security.JwtTokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/token")
    @SecurityRequirements   // io.swagger.v3.oas.annotations.security.SecurityRequirements: empty = public
    public TokenResponse token(@Valid @RequestBody TokenRequest tokenRequest) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(tokenRequest.email(), tokenRequest.password()));

        JwtTokenService.IssuedToken token = jwtTokenService.issue(authentication);
        return new TokenResponse(token.value(), "Bearer", token.expiresInSeconds());
    }
}
