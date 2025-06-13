package com.example.authswitch.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/transaction")
public class InfoController {

    @Value("${app.environment}")
    private String environment;

    @GetMapping("/info")
    public String getEnvironmentInfo() {
        return "Running in environment: " + environment;
    }
}