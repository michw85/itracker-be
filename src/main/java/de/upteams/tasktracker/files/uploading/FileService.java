package de.upteams.tasktracker.files.uploading;

import org.springframework.scheduling.annotation.Async;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for asynchronous uploading of files to an S3-compatible storage (e.g., DigitalOcean Spaces).
 * <p>
 * Provides a method for uploading a file stream to a specified object key in the target bucket.
 * The method returns a CompletableFuture indicating success ({@code true}) or failure ({@code false}).
 * </p>
 * <p>
 * <strong>Note:</strong> To enable asynchronous execution of {@link #uploadFileAsync},
 * ensure that Spring's async support is configured (e.g., with {@code @EnableAsync}
 * and a suitable ExecutorService bean).
 * </p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * @Autowired
 * private FileService fileService;
 *
 * try (InputStream in = new FileInputStream("localfile.txt")) {
 *     long size = new File("localfile.txt").length();
 *     CompletableFuture<Boolean> future = fileService.uploadFileAsync(
 *         "remote/path/file.txt",
 *         in,
 *         Map.of("uploadedBy", "user123"),
 *         "text/plain",
 *         size,
 *         true
 *     );
 *     future.thenAccept(success -> {
 *         if (success) System.out.println("Upload completed");
 *         else System.err.println("Upload failed");
 *     });
 * } catch (IOException e) {
 *     e.printStackTrace();
 * }
 * }</pre>
 *
 */
public interface FileService {

    /**
     * Asynchronously uploads a file stream to the configured bucket under the given object key.
     *
     * @param objectKey     the target key (path) under which to store the file; must not be null or blank
     * @param inputStream   the file content as an InputStream; will be closed after upload
     * @param metadata      optional metadata to attach to the object (may be null or empty)
     * @param contentType   optional MIME type of the file (e.g., "image/png"); may be null or blank
     * @param contentLength exact size in bytes of the input stream; must be greater than zero
     * @param isPublicRead  if true, assigns PUBLIC_READ ACL; otherwise PRIVATE
     * @return a CompletableFuture that completes with true if the upload succeeded,
     *         or false if validation fails or an exception occurs
     * @throws IllegalArgumentException if objectKey is null/blank or contentLength is null/≤0
     */
    @Async
    CompletableFuture<Boolean> uploadFileAsync(
            String objectKey,
            InputStream inputStream,
            Map<String, String> metadata,
            String contentType,
            Long contentLength,
            boolean isPublicRead
    );
}
