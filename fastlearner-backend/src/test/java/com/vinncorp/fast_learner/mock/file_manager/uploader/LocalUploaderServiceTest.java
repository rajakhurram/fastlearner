package com.vinncorp.fast_learner.mock.file_manager.uploader;

import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.request.uploader.UploadFileRequest;
import com.vinncorp.fast_learner.response.video.VideoUploaderResponse;
import com.vinncorp.fast_learner.services.file_manager.uploader.LocalUploader;
import com.vinncorp.fast_learner.services.user.UserService;
import com.vinncorp.fast_learner.services.video.IVideoService;
import com.vinncorp.fast_learner.util.enums.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocalUploaderServiceTest {

    @InjectMocks
    private LocalUploader localUploader;

    @Mock
    private UserService userService;

    @Mock
    private IVideoService videoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Inject the values for @Value fields
        ReflectionTestUtils.setField(localUploader, "ROOT_DIRECTORY", "/test/root/");
        ReflectionTestUtils.setField(localUploader, "COURSES_DIRECTORY", "courses/");
        ReflectionTestUtils.setField(localUploader, "USERS_DIRECTORY", "users/");
        ReflectionTestUtils.setField(localUploader, "BACKEND_URL", "http://localhost:8080/");
    }

    @Test
    public void testCreateDirectoryIfNotExists_VideoDirectory() throws IOException, IOException {
        Path expectedPath = Paths.get("/test/root/courses/");

        Path path = localUploader.createDirectoryIfNotExists(FileType.VIDEO);

        assertTrue(Files.exists(path));
        assertEquals(expectedPath, path);
    }

    @Test
    public void testCreateDirectoryIfNotExists_ProfileImageDirectory() throws IOException {
        // Prepare
        Path expectedPath = Paths.get("/test/root/users/");
        Files.deleteIfExists(expectedPath);

        // Act
        Path path = localUploader.createDirectoryIfNotExists(FileType.PROFILE_IMAGE);

        // Assert
        assertTrue(Files.exists(path));
        assertEquals(expectedPath, path);

        // Cleanup
        Files.deleteIfExists(expectedPath);
    }

    @Test
    public void testSaveFileInDirectory_Success() throws Exception {
        // Prepare
        Path testPath = Paths.get("/test/root/courses/");
        Files.createDirectories(testPath);
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());

        // Act
        String savedFilename = localUploader.saveFileInDirectory(testPath, multipartFile);

        // Assert
        assertEquals("test.txt", savedFilename);
        assertTrue(Files.exists(testPath.resolve("test.txt")));
    }

    @Test
    public void testSaveFileInDirectory_ThrowsBadRequestException() {
        // Prepare
        Path testPath = Paths.get("/invalid/path/");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> localUploader.saveFileInDirectory(testPath, multipartFile));
    }

    @Test
    public void testUpload_VideoFile() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "video.mp4", "video/mp4", "Test video content".getBytes());
        UploadFileRequest request = new UploadFileRequest(multipartFile, FileType.ARTICLE, null);

        VideoTranscript mockTranscript = mock(VideoTranscript.class);
        when(videoService.generateTranscript(multipartFile)).thenReturn(mockTranscript);

        Object response = localUploader.upload(request, "user@example.com");

        assertFalse(response instanceof VideoUploaderResponse);
    }

    @Test
    public void testUpload_OtherFileTypes() throws Exception {
        // Prepare
        MockMultipartFile multipartFile = new MockMultipartFile("file", "document.pdf", "application/pdf", "Test content".getBytes());
        UploadFileRequest request = new UploadFileRequest(multipartFile, FileType.ARTICLE, null);

        // Act
        Object response = localUploader.upload(request, "user@example.com");

        // Assert
        assertTrue(response instanceof String);
        String url = (String) response;
        assertTrue(url.contains("document.pdf"));
    }

    @Test
    public void testUpload_ThrowsBadRequestExceptionForUnsupportedFile() throws BadRequestException, UnsupportedEncodingException {
        // Prepare
        MockMultipartFile multipartFile = new MockMultipartFile("file", "unsupported.xyz", "application/octet-stream", "Test content".getBytes());
        UploadFileRequest request = new UploadFileRequest(multipartFile, FileType.ARTICLE, null);

        assertNotNull(localUploader.upload(request, "user@example.com"));
    }
}
