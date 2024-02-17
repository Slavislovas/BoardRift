package com.socialnetwork.boardrift.config;

import com.socialnetwork.boardrift.config.filter.JWTAuthenticationFilter;
import com.socialnetwork.boardrift.enumeration.Role;
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
                            .requestMatchers(HttpMethod.POST, "**/auth/refresh-token").permitAll()
                            .requestMatchers("/error").permitAll()
                            .requestMatchers(HttpMethod.POST, "**/users/**/friend-requests/send").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .requestMatchers(HttpMethod.POST, "**/users/**/friend-requests/accept").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .requestMatchers(HttpMethod.DELETE, "**/users/**/friend-requests/decline").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .requestMatchers(HttpMethod.GET, "**/users/friend-requests/sent").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .requestMatchers(HttpMethod.GET, "**/users/friend-requests/received").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .requestMatchers(HttpMethod.GET, "**/users/**/friends").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .requestMatchers(HttpMethod.GET, "**/users/**/friends/search**").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .requestMatchers(HttpMethod.GET, "**/messages/**/**").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .requestMatchers(HttpMethod.POST, "**/posts/played-game**").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .requestMatchers(HttpMethod.POST, "**/posts/simple**").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .requestMatchers(HttpMethod.POST, "**/posts/poll**").hasAnyRole("USER", "CONTENT_CURATOR", "ADMINISTRATOR")
                            .anyRequest()
                            .authenticated();
                })
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
