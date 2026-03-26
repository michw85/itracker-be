package de.upteams.tasktracker.files.uploading;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String upload(MultipartFile file) {
        try {

            File tempFile = File.createTempFile("avatar-", file.getOriginalFilename());
            file.transferTo(tempFile);

            Map<?, ?> uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());

            tempFile.delete();
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }
}

