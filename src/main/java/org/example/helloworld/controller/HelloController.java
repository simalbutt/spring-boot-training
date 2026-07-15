package org.example.helloworld.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${app.message.welcome}")
    private String welcomeMessage;

    @GetMapping("/")
    public String hello() {
        return welcomeMessage;
    }
}