package com.taller.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test-open")
    public Map<String, String> testOpen() {
        return Map.of("status", "ok");
    }
}