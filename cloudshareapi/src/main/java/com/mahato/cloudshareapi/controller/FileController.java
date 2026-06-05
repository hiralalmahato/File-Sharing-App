package com.mahato.cloudshareapi.controller;



import com.mahato.cloudshareapi.document.UserCredits;
import com.mahato.cloudshareapi.dto.FileMetadataDTO;
import com.mahato.cloudshareapi.service.FileMetadataService;
import com.mahato.cloudshareapi.service.UserCreditsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileMetadataService fileMetadataService;
    private final UserCreditsService userCreditsService;

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

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
        try {
            FileMetadataDTO downloadableFile = fileMetadataService.getDownloadableFile(id);
            byte[] fileBytes = fileMetadataService.downloadFileBytes(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadableFile.getName() + "\"")
                    .body(fileBytes);
        } catch (RuntimeException ex) {
            log.error("Download failed for id {}: {}", id, ex.getMessage(), ex);
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("file not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", "Unable to fetch file from storage"));
        }
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> view(@PathVariable String id) {
        try {
            FileMetadataDTO downloadableFile = fileMetadataService.getDownloadableFile(id);
            byte[] fileBytes = fileMetadataService.downloadFileBytes(id);

            MediaType mediaType = fileMetadataService.resolveMediaType(downloadableFile.getType());

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + downloadableFile.getName() + "\"")
                    .body(fileBytes);
        } catch (RuntimeException ex) {
            log.error("View failed for id {}: {}", id, ex.getMessage(), ex);
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("file not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", "Unable to fetch file from storage"));
        }
    }
    @GetMapping("/signed-url/{id}")
    public ResponseEntity<?> signedUrl(@PathVariable String id) {
        try {
            String url = fileMetadataService.getSignedUrlForDownload(id);
            if (url == null || url.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", "Unable to build download URL"));
            }

            // Verify the signed URL is actually reachable before returning it to the client.
            boolean ok = fileMetadataService.isUrlAccessible(url);
            if (!ok) {
                log.warn("Signed URL for {} was not reachable, returning error to trigger fallback", id);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", "Download URL not reachable"));
            }

            return ResponseEntity.ok(Map.of("url", url));
        } catch (RuntimeException ex) {
            log.error("Signed URL generation failed for id {}: {}", id, ex.getMessage(), ex);
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("file not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", "Unable to build download URL"));
        }
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
