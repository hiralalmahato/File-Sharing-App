package com.mahato.cloudshareapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mahato.cloudshareapi.document.FileMetadataDocument;
import com.mahato.cloudshareapi.document.ProfileDocument;
import com.mahato.cloudshareapi.dto.FileMetadataDTO;
import com.mahato.cloudshareapi.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public Resource getFileResource(String fileUrl) {
        try {
            Resource resource = new UrlResource(fileUrl);
            if (!resource.exists()) {
                throw new RuntimeException("File not found in Cloudinary");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid file URL", e);
        }
    }
}
