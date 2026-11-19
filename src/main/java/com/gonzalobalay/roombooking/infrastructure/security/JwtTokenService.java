package com.gonzalobalay.roombooking.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class JwtTokenService {

    private static final String ISSUER = "room-booking";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);

    private final JwtEncoder jwtEncoder;
    private final Clock clock;

    public JwtTokenService(JwtEncoder jwtEncoder, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
    }

    public IssuedToken issue(Authentication authentication) {
        Instant now = clock.instant();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)          // "ROLE_ADMIN"
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))  // "ADMIN"
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(TOKEN_TTL))
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new IssuedToken(token, TOKEN_TTL.toSeconds());
    }

    public record IssuedToken(String value, long expiresInSeconds) {}
}