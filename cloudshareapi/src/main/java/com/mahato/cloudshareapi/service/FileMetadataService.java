package com.mahato.cloudshareapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mahato.cloudshareapi.document.FileMetadataDocument;
import com.mahato.cloudshareapi.document.ProfileDocument;
import com.mahato.cloudshareapi.dto.FileMetadataDTO;
import com.mahato.cloudshareapi.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMetadataService {


    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final FileMetadataRepository fileMetadataRepository;
    private final Cloudinary cloudinary;

    public List<FileMetadataDTO> uploadFiles(MultipartFile files[]) throws IOException {
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        // Testing-mode fallback to avoid null pointer when auth context is unavailable.
        String clerkId = (currentProfile != null) ? currentProfile.getClerkId() : "test-user";
        if (clerkId == null || clerkId.isBlank()) {
            clerkId = "test-user";
        }

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("At least one file is required for upload");
        }

        List<FileMetadataDocument> savedFiles = new ArrayList<>();

        if(!userCreditsService.hasEnoughCredits(files.length)){
             throw new RuntimeException("Not Enough Credits to upload files please purchase more credits");
        }

        for (MultipartFile file : files){
            Map<?, ?> uploadResult;
            try {
            uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", "cloudshare/uploads",
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false
                )
            );
            } catch (Exception ex) {
            throw new RuntimeException("Cloudinary upload failed", ex);
            }

            String secureUrl = String.valueOf(uploadResult.get("secure_url"));
            String publicId = String.valueOf(uploadResult.get("public_id"));

            FileMetadataDocument fileMetaData = FileMetadataDocument.builder()
                .fileLocation(secureUrl)
                .cloudinaryPublicId(publicId)
                    .name(file.getOriginalFilename())
                    .size(file.getSize())
                    .type(file.getContentType())
                    .clerkId(clerkId)
                    .isPublic(false)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            userCreditsService.consumeCredit();

            savedFiles.add(fileMetadataRepository.save(fileMetaData));

        }
        return savedFiles.stream().map(fileMetadataDocument -> mapToDTO(fileMetadataDocument))
                .collect(Collectors.toList());
    }

    private FileMetadataDTO mapToDTO(FileMetadataDocument fileMetadataDocument) {
        return FileMetadataDTO.builder()
                .id(fileMetadataDocument.getId())
                .fileLocation(fileMetadataDocument.getFileLocation())
                .name(fileMetadataDocument.getName())
                .size(fileMetadataDocument.getSize())
                .type(fileMetadataDocument.getType())
                .clerkId(fileMetadataDocument.getClerkId())
                .isPublic(fileMetadataDocument.getIsPublic())
                .uploadedAt(fileMetadataDocument.getUploadedAt())
                .build();
    }
    public List<FileMetadataDTO> getFiles(){
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        // Testing-mode fallback to keep /files/my usable with permitAll security.
        String clerkId = (currentProfile != null) ? currentProfile.getClerkId() : "test-user";
        if (clerkId == null || clerkId.isBlank()) {
            clerkId = "test-user";
        }

        List<FileMetadataDocument> files = fileMetadataRepository.findByClerkId(clerkId);
        return files.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    public FileMetadataDTO getPublicFile(String id){
        Optional<FileMetadataDocument> fileOptional = fileMetadataRepository.findById(id);
        if(fileOptional.isEmpty() || !fileOptional.get().getIsPublic()){
            throw  new RuntimeException("Unable to get the file");

        }
        FileMetadataDocument document = fileOptional.get();
        return mapToDTO(document);

    }
    public FileMetadataDTO getDownloadableFile(String id){
        FileMetadataDocument file = fileMetadataRepository.findById(id).orElseThrow(() ->new RuntimeException("File not found"));
        return mapToDTO(file);
    }

    public String getDownloadableFileUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("File URL is missing");
        }

        if (fileUrl.contains("/fl_attachment/")) {
            return fileUrl;
        }

        int uploadIndex = fileUrl.indexOf("/upload/");
        if (uploadIndex < 0) {
            return fileUrl;
        }

        return fileUrl.replace("/upload/", "/upload/fl_attachment/");
    }

    public byte[] downloadFileBytes(String fileId) {
        FileMetadataDocument file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Set<String> candidateUrls = new LinkedHashSet<>();
        if (file.getFileLocation() != null && !file.getFileLocation().isBlank()) {
            candidateUrls.add(file.getFileLocation());
            candidateUrls.add(getDownloadableFileUrl(file.getFileLocation()));
        }

        String signedUrl = buildSignedCloudinaryUrl(file);
        if (signedUrl != null && !signedUrl.isBlank()) {
            candidateUrls.add(signedUrl);
            candidateUrls.add(getDownloadableFileUrl(signedUrl));
        }

        RuntimeException lastError = null;
        for (String candidateUrl : candidateUrls) {
            try {
                return fetchBytes(candidateUrl);
            } catch (RuntimeException ex) {
                lastError = ex;
            }
        }

        throw new RuntimeException("Unable to download file from storage", lastError);
    }

    private byte[] fetchBytes(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("File URL is missing");
        }

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new RuntimeException("Storage responded with HTTP " + responseCode);
            }

            try (InputStream inputStream = connection.getInputStream()) {
                byte[] bytes = inputStream.readAllBytes();
                if (bytes.length == 0) {
                    throw new RuntimeException("Empty response from file storage");
                }
                return bytes;
            } finally {
                connection.disconnect();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Unable to download file from storage", ex);
        }
    }

    public MediaType resolveMediaType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(mimeType);
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String buildSignedCloudinaryUrl(FileMetadataDocument file) {
        if (file.getCloudinaryPublicId() == null || file.getCloudinaryPublicId().isBlank()) {
            return null;
        }

        String resourceType = resolveResourceType(file.getFileLocation(), file.getType());
        String format = resolveFormat(file.getName(), file.getFileLocation());
        Integer version = resolveVersion(file.getFileLocation());

        var urlBuilder = cloudinary.url()
                .signed(true)
                .resourceType(resourceType)
                .type("upload");

        if (version != null) {
            urlBuilder.version(version);
        }

        if (format != null && !format.isBlank()) {
            urlBuilder.format(format);
        }

        return urlBuilder.generate(file.getCloudinaryPublicId());
    }

    private String resolveResourceType(String fileUrl, String mimeType) {
        if (fileUrl != null && !fileUrl.isBlank()) {
            Matcher matcher = Pattern.compile("/(image|video|raw)/upload/").matcher(fileUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        if (mimeType == null || mimeType.isBlank()) {
            return "raw";
        }

        String lower = mimeType.toLowerCase();
        if (lower.startsWith("image/")) {
            return "image";
        }
        if (lower.startsWith("video/")) {
            return "video";
        }
        return "raw";
    }

    private String resolveFormat(String fileName, String fileUrl) {
        String source = (fileName != null && !fileName.isBlank()) ? fileName : fileUrl;
        if (source == null || source.isBlank() || !source.contains(".")) {
            return null;
        }

        String extension = source.substring(source.lastIndexOf('.') + 1);
        if (extension.contains("/")) {
            return null;
        }
        return extension;
    }

    private Integer resolveVersion(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }

        Matcher matcher = Pattern.compile("/v(\\d+)/").matcher(fileUrl);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    public void deleteFile(String id){
        try{
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            // Testing-mode fallback to prevent null-related crashes.
            String clerkId = (currentProfile != null) ? currentProfile.getClerkId() : "test-user";
            if (clerkId == null || clerkId.isBlank()) {
                clerkId = "test-user";
            }

            FileMetadataDocument file= fileMetadataRepository.findById(id)
                    .orElseThrow(()->new RuntimeException("File not found"));

            if (file.getClerkId() == null || file.getClerkId().isBlank()) {
                throw new IllegalStateException("File owner metadata is missing");
            }

            if(!file.getClerkId().equals(clerkId)){
                throw new RuntimeException("File is not belong to current user");
            }

            if (file.getCloudinaryPublicId() != null && !file.getCloudinaryPublicId().isBlank()) {
                try {
                    cloudinary.uploader().destroy(
                            file.getCloudinaryPublicId(),
                            ObjectUtils.asMap(
                                    "resource_type", "auto",
                                    "invalidate", true
                            )
                    );
                } catch (Exception ex) {
                    throw new RuntimeException("Error deleting file from Cloudinary", ex);
                }
            }

            fileMetadataRepository.deleteById(id);
        }catch(Exception e){
            throw  new RuntimeException("Error deleting the file");

        }
    }

    public FileMetadataDTO togglePublic(String id){
        FileMetadataDocument file = fileMetadataRepository.findById(id)
                .orElseThrow(() ->new RuntimeException("File not Found"));

        file.setIsPublic(!file.getIsPublic());
        fileMetadataRepository.save(file);
        return mapToDTO(file);
    }

}
