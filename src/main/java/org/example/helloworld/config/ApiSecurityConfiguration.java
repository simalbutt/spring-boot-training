package org.example.helloworld.config;

import org.example.helloworld.user.UserService;
import org.example.helloworld.user.user;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.util.Arrays;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class ApiSecurityConfiguration {

    private final UserService userService;
    public ApiSecurityConfiguration(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(config -> config
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll().requestMatchers(HttpMethod.GET, "/api/v1/news", "/api/v1/news/**").permitAll()
//              .requestMatchers(HttpMethod.POST, "/api/v1/news", "/api/v1/news/**").hasAnyRole("reporter", "editor")
//              .requestMatchers(HttpMethod.DELETE, "/api/v1/news/**").hasAnyRole("editor")
//              .requestMatchers(HttpMethod.PUT, "/api/v1/news/**").hasAnyRole("reporter")
                        .anyRequest().authenticated())
                .formLogin(config -> config.successHandler((request, response, authentication) -> {
                    user user = userService.generateToken(authentication.getName());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"access_token\":\"" + user.getToken() + "\"}");
                }))
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
//                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(config -> config
                        .opaqueToken(opaque -> opaque
                                .introspector(token -> {
                                    user user = userService.findByToken(token);
                                    return new DefaultOAuth2AuthenticatedPrincipal(
                                            Map.of("sub", user.getUsername()),
                                            AuthorityUtils.createAuthorityList(
                                                    Arrays.stream(user.getRole().split(","))
                                                            .map(role -> "ROLE_" + role)
                                                            .toArray(String[]::new)
                                            )
                                    );
                                })
                        )
                );
        return http.build();

    }
}
