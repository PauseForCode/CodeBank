package com.code.bank.codebank.http.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> healthcheck() {
        return Map.of(
                "status", "Codebank API is Healthy"
        );
    }
}
