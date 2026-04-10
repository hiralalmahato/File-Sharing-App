package com.mahato.cloudshareapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "CloudShare API Backend is running");
        response.put("version", "1.0.0");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "CloudShare API Backend is running");
        response.put("version", "1.0.0");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
