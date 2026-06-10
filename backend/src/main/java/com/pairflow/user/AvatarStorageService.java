package com.pairflow.user;

import com.pairflow.common.error.ApiException;
import com.pairflow.common.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Avatars live in their own {@code <upload-dir>/avatars} directory and are served
 * <em>publicly</em> (see {@link AvatarController}). Keeping them separate from the
 * couple-photo storage means the public endpoint can never be used to reach a
 * private photo by guessing its id.
 */
@Service
public class AvatarStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5 MB

    private final Path root;

    public AvatarStorageService(@Value("${pairflow.storage.upload-dir}") String dir) throws IOException {
        this.root = Paths.get(dir).resolve("avatars").toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("File is empty");
        }
        if (file.getSize() > MAX_BYTES) {
            throw ApiException.badRequest("Image too large (max 5MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw ApiException.badRequest("File must be an image");
        }
        String original = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        String ext = extensionOf(original);
        if (!ALLOWED_EXT.contains(ext)) {
            throw ApiException.badRequest("Unsupported image type: " + ext);
        }
        String filename = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(filename).normalize();
        if (!target.getParent().equals(root)) {
            throw ApiException.badRequest("Invalid filename");
        }
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Failed to store file");
        }
        return filename;
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = root.resolve(filename).normalize();
            if (!file.getParent().equals(root)) {
                throw ApiException.notFound("File not found");
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw ApiException.notFound("File not found");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw ApiException.notFound("File not found");
        }
    }

    public String contentType(String filename) {
        return switch (extensionOf(filename)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }

    private String extensionOf(String name) {
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i + 1).toLowerCase() : "";
    }
}
