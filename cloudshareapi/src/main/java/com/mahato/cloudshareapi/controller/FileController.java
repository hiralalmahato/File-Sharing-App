package com.mahato.cloudshareapi.controller;



import com.mahato.cloudshareapi.document.UserCredits;
import com.mahato.cloudshareapi.dto.FileMetadataDTO;
import com.mahato.cloudshareapi.service.FileMetadataService;
import com.mahato.cloudshareapi.service.UserCreditsService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileMetadataService fileMetadataService;
    private final UserCreditsService userCreditsService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestPart("files") MultipartFile files[]) throws IOException {
        Map<String,Object> response = new HashMap<>();
        List<FileMetadataDTO> list = fileMetadataService.uploadFiles(files);

        UserCredits finalCredits = userCreditsService.getUserCredits();

        response.put("files",list);
        response.put("remainingCredits", finalCredits.getCredits());

        return ResponseEntity.ok(response);

    }

    @GetMapping("/my")
    public ResponseEntity<?> getFilesForCurrentUser(){
        List<FileMetadataDTO> files = fileMetadataService.getFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicFile(@PathVariable String id){
        FileMetadataDTO file = fileMetadataService.getPublicFile(id);
        return ResponseEntity.ok(file);
    }
    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable String id) {
        FileMetadataDTO downloadableFile = fileMetadataService.getDownloadableFile(id);
        String downloadUrl = fileMetadataService.getDownloadableFileUrl(downloadableFile.getFileLocation());
        // Redirect to Cloudinary in attachment mode so PDFs and other files download instead of
        // opening in the browser's built-in viewer.
        return ResponseEntity.status(302)
            .header(HttpHeaders.LOCATION, downloadUrl)
                .build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id){
        fileMetadataService.deleteFile(id);
        return ResponseEntity.noContent().build();

    }
    @PatchMapping("/{id}/toggle-public")
    public ResponseEntity<?> togglePublic(@PathVariable String id){
        FileMetadataDTO file = fileMetadataService.togglePublic(id);
        return ResponseEntity.ok(file);

    }
}
