package com.example.interviewback;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    @GetMapping("/hello")
    public Greeting hello() {
        return new Greeting("Hello world");
    }
}