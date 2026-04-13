package com.mahato.cloudshareapi.controller;

import com.mahato.cloudshareapi.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@CrossOrigin("*")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadFile(file);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "File uploaded successfully");
        response.put("url", url);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(@RequestParam("file") MultipartFile[] files) {
        List<String> urls = fileUploadService.uploadFiles(files);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Files uploaded successfully");
        response.put("urls", urls);
        return ResponseEntity.ok(response);
    }
}
