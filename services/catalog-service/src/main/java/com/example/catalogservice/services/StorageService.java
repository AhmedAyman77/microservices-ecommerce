package com.example.catalogservice.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class StorageService {
    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile image) {
        try {
            Map result = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
            return (String) result.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }
}
