
package com.focusforge.focusforge.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final long TOKEN_VALIDITY_SECONDS = 30 * 60;

    private final JwtEncoder jwtEncoder;

    public JwtService(
            @Value("${focusforge.jwt.secret}") String encodedSecret) {

        byte[] keyBytes = Base64.getDecoder().decode(encodedSecret);

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "JWT secret must decode to exactly 32 bytes.");
        }

        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        this.jwtEncoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey));
    }

    public String generateToken(Long userId, String email) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("focusforge")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(TOKEN_VALIDITY_SECONDS))
                .subject(userId.toString())
                .claim("email", email)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
