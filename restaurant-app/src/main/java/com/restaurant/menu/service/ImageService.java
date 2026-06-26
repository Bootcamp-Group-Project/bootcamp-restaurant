package com.restaurant.menu.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    String uploadImage(MultipartFile file, String prefix) throws IOException;
}
