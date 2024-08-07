package com.socialnetwork.boardrift.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AwsService {
    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    public String uploadProfilePicture(Long userId, MultipartFile file) {
        String key = "user_profile_pictures/" + UUID.randomUUID() + userId + ".jpg";
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpeg");

            s3Client.putObject(bucketName, key, file.getInputStream(), metadata);
        } catch (Exception e) {
            return null;
        }
        return s3Client.getUrl(bucketName, key).toString();
    }

   public void deleteProfilePicture(String profilePictureUrl) {
        String[] parts = profilePictureUrl.split("/");
        String key = "user_profile_pictures/" + parts[4];
        s3Client.deleteObject(bucketName, key);
   }
}
