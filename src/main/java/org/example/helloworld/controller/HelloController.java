package org.example.helloworld.controller;

import org.example.helloworld.News.NewsService;
import org.example.helloworld.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${app.message.welcome}")
    private String welcomeMessage;

    @Autowired
    private UserService userService;

    @Autowired
    private NewsService newsService;

    @GetMapping("/")
    public String hello() {
        userService.findByUsername("reporter");
        newsService.report();
        return welcomeMessage;
    }
}