package com.moaskkyo.server.global.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moaskkyo.server.global.config.JwtConfig;
import com.moaskkyo.server.global.error.BusinessException;
import com.moaskkyo.server.global.error.ErrorCode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-only-0123456789";

    private JwtProperties properties;
    private JwtEncoder encoder;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties(SECRET, 3600, 1209600);
        JwtConfig config = new JwtConfig();
        SecretKey key = config.jwtSecretKey(properties);
        encoder = config.jwtEncoder(key);
        decoder = config.jwtDecoder(key);
    }

    private JwtTokenProvider providerAt(Instant now) {
        return new JwtTokenProvider(encoder, decoder, properties, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("액세스 토큰은 1시간, 리프레시 토큰은 14일이고 사용자 ID와 역할이 들어 있다")
    void issue() {
        JwtTokenProvider provider = providerAt(Instant.now());

        IssuedTokens tokens = provider.issue(101L, Role.STUDENT);

        assertThat(tokens.expiresInSeconds()).isEqualTo(3600);
        assertThat(tokens.refreshExpiresInSeconds()).isEqualTo(1209600);

        Jwt access = decoder.decode(tokens.accessToken());
        assertThat(access.getSubject()).isEqualTo("101");
        assertThat(access.getClaimAsString(JwtTokenProvider.CLAIM_ROLE)).isEqualTo("STUDENT");
        assertThat(access.getClaimAsString(JwtTokenProvider.CLAIM_TYPE)).isEqualTo(JwtTokenProvider.TYPE_ACCESS);
        assertThat(Duration.between(access.getIssuedAt(), access.getExpiresAt())).isEqualTo(Duration.ofHours(1));

        Jwt refresh = decoder.decode(tokens.refreshToken());
        assertThat(refresh.getClaimAsString(JwtTokenProvider.CLAIM_TYPE)).isEqualTo(JwtTokenProvider.TYPE_REFRESH);
        assertThat(Duration.between(refresh.getIssuedAt(), refresh.getExpiresAt())).isEqualTo(Duration.ofDays(14));
    }

    @Test
    @DisplayName("리프레시 토큰을 읽으면 사용자 ID와 역할을 돌려준다")
    void parseRefreshToken() {
        JwtTokenProvider provider = providerAt(Instant.now());
        IssuedTokens tokens = provider.issue(101L, Role.STUDENT);

        JwtTokenProvider.RefreshTokenClaims claims = provider.parseRefreshToken(tokens.refreshToken());

        assertThat(claims.userId()).isEqualTo(101L);
        assertThat(claims.role()).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("액세스 토큰은 리프레시 토큰으로 쓸 수 없다")
    void accessTokenIsNotRefreshToken() {
        JwtTokenProvider provider = providerAt(Instant.now());
        IssuedTokens tokens = provider.issue(101L, Role.STUDENT);

        assertThatThrownBy(() -> provider.parseRefreshToken(tokens.accessToken()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰은 거절한다")
    void expiredRefreshToken() {

        JwtTokenProvider past = providerAt(Instant.now().minus(Duration.ofDays(15)));
        IssuedTokens tokens = past.issue(101L, Role.STUDENT);
        JwtTokenProvider now = providerAt(Instant.now());

        assertThatThrownBy(() -> now.parseRefreshToken(tokens.refreshToken()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("형식이 틀린 토큰은 거절한다")
    void malformedToken() {
        JwtTokenProvider provider = providerAt(Instant.now());

        assertThatThrownBy(() -> provider.parseRefreshToken("not-a-jwt"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("서명이 다른 토큰은 거절한다")
    void tamperedSignature() {
        JwtTokenProvider provider = providerAt(Instant.now());
        String first = provider.issue(1L, Role.STUDENT).refreshToken();
        String second = provider.issue(2L, Role.STUDENT).refreshToken();

        String tampered = first.substring(0, first.lastIndexOf('.') + 1)
                + second.substring(second.lastIndexOf('.') + 1);

        assertThatThrownBy(() -> provider.parseRefreshToken(tampered))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("jwt.secret이 32바이트보다 짧으면 시작하지 않는다")
    void shortSecret() {
        JwtProperties shortSecret = new JwtProperties("short", 3600, 1209600);

        assertThatThrownBy(() -> new JwtConfig().jwtSecretKey(shortSecret))
                .isInstanceOf(IllegalStateException.class);
    }
}
