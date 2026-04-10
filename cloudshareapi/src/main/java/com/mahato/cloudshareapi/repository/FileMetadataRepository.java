package com.mahato.cloudshareapi.repository;

import com.mahato.cloudshareapi.document.FileMetadataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileMetadataRepository extends MongoRepository<FileMetadataDocument, String> {

    List<FileMetadataDocument> findByClerkId(String clerkId);

    Long countByClerkId(String clerkId);

}
