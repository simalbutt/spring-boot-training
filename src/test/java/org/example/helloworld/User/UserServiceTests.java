package org.example.helloworld.User;

import org.example.helloworld.user.User;
import org.example.helloworld.user.UserRepository;
import org.example.helloworld.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserServiceTests {
    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;


    @BeforeEach
    void setup(){
        userRepository.deleteAll();
    }


    @Test
    void shouldCreateNewUser(){

        User user = userService.findOrCreateUser("simalbutt");

        assertEquals("simalbutt", user.getUsername());
        assertEquals("ROLE_REPORTER", user.getRole());

        assertTrue(
                userRepository.findByUsername("simalbutt").isPresent()
        );
    }


    @Test
    void shouldReturnExistingUser(){

        User user = new User();

        user.setUsername("reporter");
        user.setPassword("encoded-password");
        user.setRole("ROLE_REPORTER");
        user.setToken("test-token");

        userRepository.save(user);


        User result =
                userService.findOrCreateUser("reporter");


        assertEquals(
                "reporter",
                result.getUsername()
        );
    }

    @Test
    void shouldLoadUserByUsername(){

        User user = new User();

        user.setUsername("john");
        user.setPassword("password");
        user.setRole("REPORTER");

        userRepository.save(user);


        UserDetails details =
                userService.loadUserByUsername("john");


        assertEquals(
                "john",
                details.getUsername()
        );
    }
}
