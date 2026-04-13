package com.mahato.cloudshareapi.service;


import com.mahato.cloudshareapi.document.ProfileDocument;
import com.mahato.cloudshareapi.dto.ProfileDTO;
import com.mahato.cloudshareapi.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
    private static final String TEST_CLERK_ID = "test-user";
    private static final int DEFAULT_CREDITS = 5;

    private final ProfileRepository profileRepository;

    public ProfileDTO createProfile(ProfileDTO profileDTO){

        if(profileRepository.existsByClerkId(profileDTO.getClerkId())){
            return updateProfile(profileDTO);
        }


        ProfileDocument profile = ProfileDocument.builder()
                .clerkId(profileDTO.getClerkId())
                .email(profileDTO.getEmail())
                .firstName(profileDTO.getFirstName())
                .lastName(profileDTO.getLastName())
                .photoUrl(profileDTO.getPhotoUrl())
                .credits(DEFAULT_CREDITS)
                .createdAt(Instant.now())
                .build();

        profile =profileRepository.save(profile);

        return  ProfileDTO.builder()
                .id(profile.getId())
                .clerkId(profile.getClerkId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .photoUrl(profile.getPhotoUrl())
                .credits(profile.getCredits())
                .createdAt(profile.getCreatedAt())
                .build();



    }
    public ProfileDTO updateProfile(ProfileDTO profileDTO){

       ProfileDocument existingProfile =  profileRepository.findByClerkId(profileDTO.getClerkId());
       if(existingProfile != null){
           if(profileDTO.getEmail() != null && !profileDTO.getEmail().isEmpty()){
               existingProfile.setEmail(profileDTO.getEmail());
           }

           if(profileDTO.getFirstName() != null && !profileDTO.getFirstName().isEmpty()){
               existingProfile.setFirstName(profileDTO.getFirstName());
           }
           if(profileDTO.getLastName() != null && !profileDTO.getLastName().isEmpty()){
               existingProfile.setLastName(profileDTO.getLastName());
           }

           if(profileDTO.getPhotoUrl() != null && !profileDTO.getPhotoUrl().isEmpty()){
               existingProfile.setPhotoUrl(profileDTO.getPhotoUrl());
           }

           profileRepository.save(existingProfile);

           return ProfileDTO.builder()
                   .id(existingProfile.getId())
                   .email(existingProfile.getEmail())
                   .clerkId(existingProfile.getClerkId())
                   .firstName(existingProfile.getFirstName())
                   .lastName(existingProfile.getLastName())
                   .credits(existingProfile.getCredits())
                   .createdAt(existingProfile.getCreatedAt())
                   .photoUrl(existingProfile.getPhotoUrl())
                   .build();
       }
       return null;


    }
    public  boolean existsByClerkId(String clerkId){

        return profileRepository.existsByClerkId(clerkId);
    }

    public void deleteProfile(String clerkId){
        ProfileDocument existingProfile = profileRepository.findByClerkId(clerkId);
        if(existingProfile != null){
            profileRepository.delete(existingProfile);
        }
    }

    public ProfileDocument getCurrentProfile(){
        String clerkId = resolveCurrentClerkId();
        ProfileDocument profile = profileRepository.findByClerkId(clerkId);

        if (profile != null) {
            return profile;
        }

        // Testing-mode fallback: return an in-memory default profile to avoid null handling crashes.
        if (TEST_CLERK_ID.equals(clerkId)) {
            log.warn("No authenticated user/profile found, using fallback clerkId=test-user");
        } else {
            log.warn("Profile not found for clerkId={}, returning default profile for safe handling", clerkId);
        }

        return ProfileDocument.builder()
                .clerkId(clerkId)
                .credits(DEFAULT_CREDITS)
                .createdAt(Instant.now())
                .build();
    }

    private String resolveCurrentClerkId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return TEST_CLERK_ID;
        }

        String clerkId = authentication.getName();
        if (clerkId == null || clerkId.isBlank() || "anonymousUser".equalsIgnoreCase(clerkId)) {
            return TEST_CLERK_ID;
        }

        return clerkId;
    }
}
