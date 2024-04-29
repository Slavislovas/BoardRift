package com.socialnetwork.boardrift.config;

import com.socialnetwork.boardrift.config.filter.JWTAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
                    authorizationManagerRequestMatcherRegistry
                            .requestMatchers(HttpMethod.POST, "**/users/register").permitAll()
                            .requestMatchers(HttpMethod.GET, "**/users/register/confirm**").permitAll()
                            .requestMatchers(HttpMethod.POST, "**/auth/login").permitAll()
                            .requestMatchers(HttpMethod.POST, "**/auth/reset-password").permitAll()
                            .requestMatchers(HttpMethod.PUT, "**/auth/reset-password").permitAll()
                            .requestMatchers(HttpMethod.DELETE, "**/auth/logout").permitAll()
                            .requestMatchers(HttpMethod.POST, "**/auth/refresh-token").permitAll()
                            .requestMatchers("/error").permitAll()
                            .requestMatchers(HttpMethod.POST, "/**/users/**/warnings").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/**/users/**/warnings/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/**/users/**/suspensions").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/**/users/**/suspensions").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/posts/**/**/reports/**").hasRole("ADMIN")
                            .anyRequest()
                            .authenticated();
                })
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
