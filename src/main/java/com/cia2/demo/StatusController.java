package com.cia2.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatusController {

    @GetMapping("/api/status")
    public Map<String, String> status() {
        return Map.of(
            "message", "Hello from the updated cloud CI/CD demo?!",
            "version", "v3"
        );
    }
}