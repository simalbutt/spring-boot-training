package org.example.helloworld.security;

import org.example.helloworld.Security.ApiSecurityService;
import org.example.helloworld.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApiSecurityServiceTests {


    @Autowired
    ApiSecurityService securityService;


    @Test
    void shouldGenerateAndVerifyToken() throws Exception {


        User user = new User();

        user.setUsername("reporter");
        user.setRole("ROLE_REPORTER");
        user.setToken("12345");


        MockHttpServletResponse response =
                new MockHttpServletResponse();


        securityService.generateToken(
                response,
                user
        );


        String json =
                response.getContentAsString();


        assertTrue(
                json.contains("access_token")
        );
    }
}
