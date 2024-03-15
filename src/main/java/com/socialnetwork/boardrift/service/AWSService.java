package com.socialnetwork.boardrift.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class AWSService {
    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    public String uploadProfilePicture(Long userId, MultipartFile file) {
        String key = "user_profile_pictures/" + userId + ".jpg";
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpeg");

            s3Client.putObject(bucketName, key, file.getInputStream(), metadata);
        } catch (Exception e) {
            return null;
        }
        return s3Client.getUrl(bucketName, key).toString();
    }

    @Cacheable(value = "profilePictureUrls", key = "#userId")
    public String getPreSignedUrl(Long userId) {
        String key = "user_profile_pictures/" + userId + ".jpg";

        if (!s3Client.doesObjectExist(bucketName, key)) {
            key = "user_profile_pictures/defaultProfilePicture.jpg";
        }

        long expirationTimeInMillis = minutesToSeconds(60L) * 1000L;
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key);

        generatePresignedUrlRequest
                .withMethod(HttpMethod.GET)
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeInMillis));

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    private int minutesToSeconds(Long minutes) {
        return Math.toIntExact(minutes * 60);
    }
}
