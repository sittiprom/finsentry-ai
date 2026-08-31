package com.finsentry.finsentry_ai.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigController {

    // Maps to the APP_DEMO_MODE environment variable via Spring Boot's
    // relaxed binding (APP_DEMO_MODE -> app.demo-mode). Set this to true
    // in your AWS deployment's environment config; leave unset (false) locally.
    @Value("${app.demo-mode:false}")
    private boolean demoMode;

    @GetMapping("/api/config")
    public Map<String, Object> getConfig() {
        return Map.of("demoMode", demoMode);
    }
}
