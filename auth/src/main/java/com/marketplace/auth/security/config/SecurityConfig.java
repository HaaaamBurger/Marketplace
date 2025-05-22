package com.marketplace.auth.security.config;

import com.marketplace.auth.security.JwtAuthenticationFilter;
import com.marketplace.auth.security.RestAccessDeniedHandler;
import com.marketplace.auth.security.RestAuthenticationEntryPoint;
import com.marketplace.usercore.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_REFRESH_TOKEN;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private final RestAccessDeniedHandler restAccessDeniedHandler;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] PERMITTED_ROUTES = new String[] {
            "/sign-in",
            "/sign-up",
            "/logout",
            "/home",
            "/error",
            "/swagger-ui/**",
            "/v3/api-docs*/**",
            "/favicon.ico",
            "/.well-known/appspecific/com.chrome.devtools.json"
    };

    private static final String[] ADMIN_ROUTES = new String[] {
            "/users/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .logout(logout -> logout.deleteCookies(COOKIE_ACCESS_TOKEN, COOKIE_REFRESH_TOKEN).logoutSuccessUrl("/home"))
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
                        .requestMatchers(PERMITTED_ROUTES).permitAll()
                        .requestMatchers(ADMIN_ROUTES).hasAuthority(UserRole.ADMIN.getAuthority())
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
