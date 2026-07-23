package org.example.helloworld.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.example.helloworld.user.User;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.example.helloworld.user.UserService;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class ApiSecurityService implements InitializingBean {
    private final UserService userService;
    private  NimbusJwtEncoder jwtEncoder;
    private  NimbusJwtDecoder jwtDecoder;
    private final SignatureAlgorithm jwtAlgorithm;

    public ApiSecurityService(UserService userService) throws Exception {
        this.userService = userService;
        this.jwtAlgorithm = SignatureAlgorithm.PS256;
    }
    @Override
    public void afterPropertiesSet() throws Exception{
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        this.jwtEncoder = NimbusJwtEncoder.withKeyPair(
                        (RSAPublicKey) keyPair.getPublic(),
                        (RSAPrivateKey) keyPair.getPrivate())
                .algorithm(this.jwtAlgorithm)
                .build();
        this.jwtDecoder = NimbusJwtDecoder.withPublicKey(
                        (RSAPublicKey) keyPair.getPublic())
                .signatureAlgorithm(this.jwtAlgorithm)
                .build();
//        String pem = "-----BEGIN PUBLIC KEY-----\n" +
//                Base64.getMimeEncoder(64, "\n".getBytes())
//                        .encodeToString(keyPair.getPublic().getEncoded()) +
//                "\n-----END PUBLIC KEY-----";
//        System.out.println(pem);

    }


    @SuppressFBWarnings(
            value = "XSS_SERVLET",
            justification = "Response contains a server-generated JWT serialized as JSON using Jackson. No user-controlled HTML is written."
    )
    public void generateToken(@NonNull HttpServletResponse res, @NonNull User user ) throws Exception{
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        JwsHeader jwsHeader= JwsHeader.with(this.jwtAlgorithm).build();
        JwtClaimsSet jwtClaimsSet= JwtClaimsSet.builder()
                .id(user.getToken())
                .subject(user.getUsername())
                .claim("scope", user.getRole())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        Jwt jwt= jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,jwtClaimsSet));
        Map<String, String> response = Map.of("access_token", jwt.getTokenValue());
        ObjectMapper objectMapper= new ObjectMapper();
        res.getWriter().write(objectMapper.writeValueAsString(response));
    }

    public void onAuthenticationSuccessOauth(HttpServletRequest req, HttpServletResponse res, @NonNull Authentication auth)
            throws Exception, ServletException {
        OAuth2User oauthUser = (OAuth2User) auth.getPrincipal();
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) auth;
        String provider = token.getAuthorizedClientRegistrationId();
        String username;
        if ("google".equals(provider)) {
            assert oauthUser != null;
            username = oauthUser.getAttribute("given_name");
        } else if ("github".equals(provider)) {
            assert oauthUser != null;
            username = oauthUser.getAttribute("login");
        } else {
            throw new RuntimeException("Unsupported provider");
        }
        User user = userService.findOrCreateUser(username);
        generateToken(res, user);
    }

    public void onAuthenticationSuccessForm(HttpServletRequest req, HttpServletResponse res, @NonNull Authentication auth)
            throws Exception, ServletException{
        User user = userService.findOrCreateUser(auth.getName());
        generateToken(res, user);

    }

     public DefaultOAuth2AuthenticatedPrincipal verify ( String token ){
        Jwt jwt= jwtDecoder.decode(token);
        String username= jwt.getSubject();
        String roles= jwt.getClaimAsString("scope");
        return new DefaultOAuth2AuthenticatedPrincipal(username, Map.of("sub", username),
                AuthorityUtils.createAuthorityList(roles.split(",")));
    }
}
