package org.example.helloworld.config;

import org.example.helloworld.user.JwtService;
import org.example.helloworld.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableMethodSecurity
public class ApiSecurityConfiguration {

    @Value("${jwt.secret}")
    private String jwtSecret;


    private final JwtService jwtService;
    private final UserService userService;
    public ApiSecurityConfiguration(JwtService jwtService, UserService userService ) {
        this.jwtService = jwtService;
        this.userService=userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(config -> config
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/news", "/api/v1/news/**").permitAll()
//              .requestMatchers(HttpMethod.POST, "/api/v1/news", "/api/v1/news/**").hasAnyRole("reporter", "editor")
//              .requestMatchers(HttpMethod.DELETE, "/api/v1/news/**").hasAnyRole("editor")
//              .requestMatchers(HttpMethod.PUT, "/api/v1/news/**").hasAnyRole("reporter")
                        .anyRequest().authenticated())
                //for custom token authenticaton
//                .formLogin(config -> config.successHandler((request, response, authentication) -> {
//                    user user = userService.generateToken(authentication.getName());
//                    response.setContentType("application/json");
//                    response.getWriter().write("{\"access_token\":\"" + user.getToken() + "\"}");
//                }))
                .formLogin(config -> config.successHandler(
                                (request, response, authentication) -> {
                                    UserDetails user = (UserDetails) authentication.getPrincipal();
                                    String token = jwtService.generateToken(user.getUsername(),
                                                    user.getAuthorities().iterator().next().getAuthority());
                                    response.setContentType("application/json");
                                    response.getWriter().write("{\"access_token\":\"" + token + "\"}");}
                        )
                )

                .oauth2Login(oauth -> oauth.successHandler(
                                (request,response,authentication)-> {
                                    OAuth2User googleUser = (OAuth2User) authentication.getPrincipal();
                                    String username = googleUser.getAttribute("given_name");
                                    userService.findOrCreateUser(username, "ROLE_REPORTER");
                                    String token = jwtService.generateToken(username, "ROLE_REPORTER");
                                    response.getWriter().write("{\"access_token\":\"" + token + "\"}");
                                }
                        )
                )
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
//                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //for the custom token authentication

//                .oauth2ResourceServer(config -> config
//                        .opaqueToken(opaque -> opaque.introspector(token -> {
//                                    user user = userService.findByToken(token);
//                                    return new DefaultOAuth2AuthenticatedPrincipal(
//                                            Map.of("sub", user.getUsername()),
//                                            AuthorityUtils.createAuthorityList(
//                                                    Arrays.stream(user.getRole().split(","))
//                                                            .map(role -> "ROLE_" + role)
//                                                            .toArray(String[]::new)
//                                            )
//                                    );
//                                })
//                        )
//                );
                .oauth2ResourceServer(config ->
                        config.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();

        converter.setAuthoritiesClaimName("role");
        converter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);


        return jwtConverter;
    }
}
