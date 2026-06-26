package com.example.canteen.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    String uploadImage(MultipartFile file, String prefix) throws IOException;
}
