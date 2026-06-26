package com.example.canteen.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalImageService implements ImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final Path uploadDir;
    private final int minPixel;
    private final int maxPixel;
    private final long maxBytes;

    public LocalImageService(
            @Value("${canteen.image.upload-dir:uploads}") String uploadDir,
            @Value("${canteen.image.min-pixel:600}") int minPixel,
            @Value("${canteen.image.max-pixel:800}") int maxPixel,
            @Value("${canteen.image.max-bytes:5242880}") long maxBytes) throws IOException {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.minPixel = minPixel;
        this.maxPixel = maxPixel;
        this.maxBytes = maxBytes;
        Files.createDirectories(this.uploadDir);
    }

    @Override
    public String uploadImage(MultipartFile file, String prefix) throws IOException {
        validate(file);
        String extension = resolveExtension(file.getOriginalFilename());
        String filename = prefix + "_" + UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(filename);

        BufferedImage source = ImageIO.read(file.getInputStream());
        if (source == null) {
            throw new IllegalArgumentException("Invalid image file");
        }

        int width = source.getWidth();
        int height = source.getHeight();
        int targetSize = calculateTargetSize(width, height);

        Thumbnails.of(source)
                .size(targetSize, targetSize)
                .keepAspectRatio(true)
                .outputFormat(extension.replace(".", ""))
                .toFile(target.toFile());

        return "/uploads/" + filename;
    }

    /** 將較長邊縮放至 600~800 之間，維持比例 */
    private int calculateTargetSize(int width, int height) {
        int longer = Math.max(width, height);
        if (longer <= minPixel) {
            return minPixel;
        }
        if (longer >= maxPixel) {
            return maxPixel;
        }
        return longer;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("Image exceeds maximum allowed size");
        }
        String extension = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported image type: " + extension);
        }
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("File must have an extension");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        return ".jpeg".equals(ext) ? ".jpg" : ext;
    }
}
