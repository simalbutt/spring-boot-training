package org.example.helloworld.config;

import org.example.helloworld.Security.ApiSecurityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableMethodSecurity
public class ApiSecurityConfiguration {

    public final ApiSecurityService securityService;

    public ApiSecurityConfiguration(ApiSecurityService securityService) {
        this.securityService=securityService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(config -> config
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/news", "/api/v1/news/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(config -> config.successHandler((request, response, authentication) -> {
                                    try {
                                        securityService.onAuthenticationSuccessForm(request,response,authentication);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )
                )
                .oauth2Login(oauth -> oauth.successHandler((request, response, authentication) -> {
                        try {
                            securityService.onAuthenticationSuccessOauth(request,response,authentication);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                }))
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(config -> config
                        .opaqueToken(config2-> config2.introspector(securityService::verify)));
        return http.build();
    }
}
