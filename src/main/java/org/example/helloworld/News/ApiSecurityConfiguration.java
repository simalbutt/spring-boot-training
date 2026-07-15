package org.example.helloworld.News;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ApiSecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(config -> config
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/news","/api/v1/news/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/v1/news","/api/v1/news/**").hasAnyRole("reporter","editor")
                .requestMatchers(HttpMethod.DELETE,"/api/v1/news/**").hasAnyRole("editor")
                        .requestMatchers(HttpMethod.PUT,"/api/v1/news/**").hasAnyRole("reporter")
                        .anyRequest().hasRole("reporter"))
                .formLogin(config ->{
                }).csrf(AbstractHttpConfigurer::disable);
        return http.build();

    }
}
