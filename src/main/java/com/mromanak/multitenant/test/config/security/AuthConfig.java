package com.mromanak.multitenant.test.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import javax.servlet.http.HttpServletRequest;

/**
 * Configures the URLs that can be accessed without OAuth2 authentication, and configures the authentication manager
 * that should be used for OAuth2.
 */
@Configuration
public class AuthConfig {

    @Bean
    public SecurityFilterChain jwtSecurityFilterChain(
            HttpSecurity http,
            AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver
    ) throws Exception {

        http.authorizeHttpRequests(a -> a

                // Configure the endpoints that can bypass authentication
                .antMatchers(
                        "/error",
                        "/favicon*",
                        "/swagger-resources/**",
                        "/swagger-ui.html",
                        "/swagger-ui*/**",
                        "/v3/api-docs*/**",
                        "/webjars/**"
                ).permitAll()

                // Authenticate all other requests
                .anyRequest().authenticated()
        ).oauth2ResourceServer(o -> o
                .authenticationManagerResolver(authenticationManagerResolver)
        );
        return http.build();
    }
}
