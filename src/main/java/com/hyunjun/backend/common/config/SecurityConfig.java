package com.hyunjun.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager
    ) throws Exception {
        http.authenticationManager(authenticationManager);

        http.authorizeHttpRequests(authorize ->
                authorize
                        .requestMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/accounts", "/auth/login").permitAll()
                        .anyRequest().authenticated());

        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                ));

        http.logout(logout ->
                logout.logoutUrl("/auth/logout")
                        .logoutSuccessHandler(
                                new HttpStatusReturningLogoutSuccessHandler(
                                        HttpStatus.NO_CONTENT
                                )
                        ));

        return http.build();
    }
}
