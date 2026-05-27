package com.capg.jobportal.util;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Component
public class CloudinaryUtil {
 
    @Value("${cloudinary.cloud-name}")
    private String cloudName;
 
    @Value("${cloudinary.api-key}")
    private String apiKey;
 
    @Value("${cloudinary.api-secret}")
    private String apiSecret;
 
    private Cloudinary getCloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
 
    public String uploadResume(MultipartFile file) throws IOException {
        validateResume(file);
 
        String uniqueFilename = UUID.randomUUID().toString() + ".pdf";
 
        Map result = getCloudinary().uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "jobportal/resumes",
                        "resource_type", "raw",
                        "public_id", uniqueFilename,
                        "access_mode", "public",
                        "type", "upload"
                )
        );
 
        return result.get("secure_url").toString();
    }
 
    private void validateResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Resume must not exceed 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed for resume");
        }
    }
}	
