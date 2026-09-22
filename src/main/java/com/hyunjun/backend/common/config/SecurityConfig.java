package com.hyunjun.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CsrfTokenRepository csrfTokenRepository,
            SecurityContextRepository securityContextRepository
    ) throws Exception {
        http.csrf(csrf -> csrf.spa().csrfTokenRepository(csrfTokenRepository));
        http.securityContext(context ->
                context.securityContextRepository(securityContextRepository));
        http.requestCache(cache -> cache.disable());

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/accounts", "/auth/login").permitAll()
                .anyRequest().hasAuthority("ACCOUNT"));

        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        http.logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessHandler(
                        new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)
                ));

        return http.build();


    }
}
