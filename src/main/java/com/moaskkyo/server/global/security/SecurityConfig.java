package com.moaskkyo.server.global.security;

import com.moaskkyo.server.global.auth.JwtTokenProvider;
import com.moaskkyo.server.global.auth.Role;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Set<String> PUBLIC_POST_PATHS = Set.of(
            "/api/user/invite/verify",
            "/api/user/signup",
            "/api/user/login",
            "/api/user/refresh");

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS.toArray(String[]::new)).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/user/**").hasRole(Role.STUDENT.name())
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(this::resolveBearerToken)
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                        .jwt(jwt -> jwt
                                .decoder(accessTokenDecoder(jwtDecoder))
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    private String resolveBearerToken(HttpServletRequest request) {
        if (HttpMethod.POST.matches(request.getMethod()) && PUBLIC_POST_PATHS.contains(request.getRequestURI())) {
            return null;
        }
        return new DefaultBearerTokenResolver().resolve(request);
    }

    private JwtDecoder accessTokenDecoder(JwtDecoder jwtDecoder) {
        return token -> {
            Jwt jwt = jwtDecoder.decode(token);
            if (!JwtTokenProvider.TYPE_ACCESS.equals(jwt.getClaimAsString(JwtTokenProvider.CLAIM_TYPE))) {
                throw new BadJwtException("액세스 토큰이 아닙니다.");
            }
            return jwt;
        };
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(JwtTokenProvider.CLAIM_ROLE);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
