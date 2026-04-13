package com.mahato.cloudshareapi.service;

import com.mahato.cloudshareapi.document.FileMetadataDocument;
import com.mahato.cloudshareapi.document.ProfileDocument;
import com.mahato.cloudshareapi.dto.FileMetadataDTO;
import com.mahato.cloudshareapi.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import com.mongodb.client.gridfs.model.GridFSFile;

@Service
@RequiredArgsConstructor
public class FileMetadataService {


    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final FileMetadataRepository fileMetadataRepository;
    private final GridFsTemplate gridFsTemplate;

    public List<FileMetadataDTO> uploadFiles(MultipartFile files[]) throws IOException {
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        List<FileMetadataDocument> savedFiles = new ArrayList<>();

        if(!userCreditsService.hasEnoughCredits(files.length)){
             throw new RuntimeException("Not Enough Credits to upload files please purchase more credits");
        }

        for (MultipartFile file : files){
            ObjectId fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType());

            FileMetadataDocument fileMetaData = FileMetadataDocument.builder()
                    .fileLocation(fileId.toString())
                    .name(file.getOriginalFilename())
                    .size(file.getSize())
                    .type(file.getContentType())
                    .clerkId(currentProfile.getClerkId())
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
        List<FileMetadataDocument> files = fileMetadataRepository.findByClerkId(currentProfile.getClerkId());
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
            FileMetadataDocument file= fileMetadataRepository.findById(id)
                    .orElseThrow(()->new RuntimeException("File not found"));
            if(!file.getClerkId().equals(currentProfile.getClerkId())){
                throw new RuntimeException("File is not belong to current user");
            }

            try {
                gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(file.getFileLocation()))));
            } catch (Exception ex) {
                // Ignore if it doesn't exist
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

    public GridFsResource getFileResource(String gridFsId) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(gridFsId))));
        if (file == null) {
            throw new RuntimeException("File not found in MongoDB GridFS");
        }
        return gridFsTemplate.getResource(file);
    }
}
