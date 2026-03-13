package de.upteams.tasktracker.user.service;

import de.upteams.tasktracker.files.uploading.FileService;
import de.upteams.tasktracker.user.dto.request.PasswordChangeDto;
import de.upteams.tasktracker.user.dto.request.ProfileUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.exception.InvalidPasswordException;
import de.upteams.tasktracker.user.util.AppUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserService userService;
    private final AppUserMapper userMapper;
    private final FileService fileService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponseDto getUserProfile(String userId) {
        AppUser user = userService.getByIdOrThrow(userId);
        return userMapper.mapEntityToDto(user);
    }

    @Transactional
    public UserResponseDto updateProfile(String userId, ProfileUpdateDto updateDto) {
        AppUser user = userService.getByIdOrThrow(userId);

        user.updateProfile(
                updateDto.displayName(),
                updateDto.position(),
                updateDto.department(),
                updateDto.bio()
        );

        userService.saveOrUpdate(user);
        log.info("User {} updated their profile", userId);

        return userMapper.mapEntityToDto(user);
    }

    @Transactional
    public UserResponseDto uploadAvatar(String userId, MultipartFile file) {
        AppUser user = userService.getByIdOrThrow(userId);

        try {
            String fileName = generateAvatarFileName(userId, file.getOriginalFilename());

            CompletableFuture<Boolean> uploadFuture = fileService.uploadFileAsync(
                    "avatars/" + fileName,
                    file.getInputStream(),
                    null,
                    file.getContentType(),
                    file.getSize(),
                    true
            );

            uploadFuture.thenAccept(success -> {
                if (success) {
                    log.info("Avatar uploaded successfully for user {}", userId);
                } else {
                    log.error("Failed to upload avatar for user {}", userId);
                }
            });

            // Set avatar URL immediately (assuming synchronous upload for demo)
            String avatarUrl = String.format("https://%s.%s/avatars/%s",
                    "test-bucket", // Should be from config
                    "fra1.digitaloceanspaces.com",
                    fileName);

            user.updateAvatar(avatarUrl);
            userService.saveOrUpdate(user);

        } catch (IOException e) {
            log.error("Failed to upload avatar for user {}", userId, e);
            throw new RuntimeException("Failed to upload avatar", e);
        }

        return userMapper.mapEntityToDto(user);
    }

    @Transactional
    public UserResponseDto deleteAvatar(String userId) {
        AppUser user = userService.getByIdOrThrow(userId);
        user.updateAvatar(null);
        userService.saveOrUpdate(user);
        log.info("Avatar deleted for user {}", userId);
        return userMapper.mapEntityToDto(user);
    }

    @jakarta.transaction.Transactional
    public void changePassword(String userId, PasswordChangeDto dto) {
        AppUser user = userService.getByIdOrThrow(userId);

        if (!passwordEncoder.matches(dto.oldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid current password");
        }

        if (passwordEncoder.matches(dto.newPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userService.saveOrUpdate(user);
        log.info("Password updated for user {}", userId);
    }

    private String generateAvatarFileName(String userId, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return userId + "_" + UUID.randomUUID() + extension;
    }
}