package com.vinncorp.fast_learner.services.file_manager.uploader;

import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.services.video.IVideoService;
import com.vinncorp.fast_learner.request.uploader.UploadFileRequest;
import com.vinncorp.fast_learner.response.video.VideoUploaderResponse;
import com.vinncorp.fast_learner.util.enums.FileType;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalUploader implements UploaderFactory {

    @Value("${directory.root}")
    private String ROOT_DIRECTORY;

    @Value("${directory.courses}")
    private String COURSES_DIRECTORY;

    @Value("${directory.users}")
    private String USERS_DIRECTORY;

    @Value("${backend.domain.url}")
    private String BACKEND_URL;

    private final IUserService userService;
    private final IVideoService videoService;

    @Override
    public Object upload(UploadFileRequest request, String email) throws BadRequestException, UnsupportedEncodingException {
        log.info("Uploading " + request+ " file started.");
        Path path = createDirectoryIfNotExists(request.getFileType());
        String filename = URLEncoder.encode(saveFileInDirectory(path, request.getFile()), "UTF-8");
        String url = APIUrls.DOWNLOADER + APIUrls.DOWNLOAD + "?filename=" + filename + "&fileType=" + request.getFileType().name();
        if (request.getFileType() == FileType.VIDEO) {
            // call to generate the transcript of the video.
            VideoTranscript videoTranscript = videoService.generateTranscript(request.getFile());
            return VideoUploaderResponse.builder()
                    .transcriptData(videoTranscript)
                    .url(url)
                    .fileName(filename)
                    .build();
        }
        return url;
    }

    // Create directory if not exists
    public Path createDirectoryIfNotExists(FileType fileType) {
        Path directory = null;
        switch (fileType) {
            case VIDEO:
            case PREVIEW_VIDEO:
            case ARTICLE:
            case PREVIEW_THUMBNAIL:
            case TRANSCRIBE:
                directory = Paths.get(ROOT_DIRECTORY + COURSES_DIRECTORY);
                break;
            case PROFILE_IMAGE:
                directory = Paths.get(ROOT_DIRECTORY + USERS_DIRECTORY);
                break;
        }

        // Check if the directory doesn't exist
        if (!Files.exists(directory)) {
            try {
                // Create the directory
                Files.createDirectories(directory);
                System.out.println("Directory created successfully.");
            } catch (IOException e) {
                System.err.println("Failed to create the directory: " + e.getMessage());
            }
        } else {
            System.out.println("Directory already exists.");
        }
        return directory;
    }

    // Saving file on to the directory created or existed
    public String saveFileInDirectory(Path path, MultipartFile multipartFile) throws BadRequestException {
        try {
            // Get the file's original name
            String originalFilename = multipartFile.getOriginalFilename();

            // Construct the full path where you want to save the file
            Path fullPath = path.resolve(originalFilename);

            // Save the file to the specified path
            Files.copy(multipartFile.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);
            return originalFilename; // Return the saved file's path
        } catch (IOException e) {
            // Handle any exceptions that may occur during the file-saving process
            e.printStackTrace();
            throw new BadRequestException("File cannot be saved");
        }
    }

    @Override
    public void delete(Long id, String url, String fileType, Long topicId) {

    }
}
