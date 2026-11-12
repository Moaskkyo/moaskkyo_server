package com.moaskkyo.server.global.auth;

import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtTokenProvider(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.properties = properties;
        this.clock = clock;
    }

    public IssuedTokens issue(Long userId, Role role) {
        Instant now = clock.instant();
        String accessToken = encode(userId, role, TYPE_ACCESS, now, properties.accessExpirationSeconds());
        String refreshToken = encode(userId, role, TYPE_REFRESH, now, properties.refreshExpirationSeconds());
        return new IssuedTokens(
                accessToken,
                properties.accessExpirationSeconds(),
                refreshToken,
                properties.refreshExpirationSeconds());
    }

    public RefreshTokenClaims parseRefreshToken(String token) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (!TYPE_REFRESH.equals(jwt.getClaimAsString(CLAIM_TYPE))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        String subject = jwt.getSubject();
        String roleClaim = jwt.getClaimAsString(CLAIM_ROLE);
        if (subject == null || roleClaim == null) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        try {
            return new RefreshTokenClaims(Long.valueOf(subject), Role.valueOf(roleClaim));
        } catch (IllegalArgumentException e) {

            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private String encode(Long userId, Role role, String type, Instant now, long expirationSeconds) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_TYPE, type)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public record RefreshTokenClaims(Long userId, Role role) {
    }
}
