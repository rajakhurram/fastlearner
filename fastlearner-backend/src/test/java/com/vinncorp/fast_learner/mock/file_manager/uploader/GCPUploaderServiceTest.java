package com.vinncorp.fast_learner.mock.file_manager.uploader;

import com.vinncorp.fast_learner.dtos.docs.DocumentSummary;
import com.vinncorp.fast_learner.dtos.file_manager.uploader.gcp.FileDto;
import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
import com.vinncorp.fast_learner.request.uploader.UploadFileRequest;
import com.vinncorp.fast_learner.response.docs.DocumentSummaryResponse;
import com.vinncorp.fast_learner.response.docs.DocumentUploaderResponse;
import com.vinncorp.fast_learner.response.video.VideoUploaderResponse;
import com.vinncorp.fast_learner.services.docs.DocumentService;
import com.vinncorp.fast_learner.services.file_manager.uploader.GCPUploader;
import com.vinncorp.fast_learner.services.user.UserCourseProgressService;
import com.vinncorp.fast_learner.services.video.VideoService;
import com.vinncorp.fast_learner.util.enums.FileType;
import com.vinncorp.fast_learner.util.gcp.DataBucketUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GCPUploaderServiceTest {

    @Mock
    private CourseRepository courseRepo;

    @Mock
    private DataBucketUtil dataBucketUtil;

    @Mock
    private DocumentService documentService;

    @Mock
    private VideoService videoService;

    @Mock
    private UserCourseProgressService userCourseProgressService;

    @InjectMocks
    private GCPUploader gcpUploader;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(gcpUploader, "GCP_BUCKET_URL", "https://bucket-url.com/");
    }

    @Test
    @DisplayName("Upload doc with success")
    public void testUpload_RegenerateSummary() throws BadRequestException, UnsupportedEncodingException {
        // Arrange
        UploadFileRequest request = new UploadFileRequest();
        request.setFile(null);
        request.setUrl("https://storage.googleapis.com/fastlearner-bucket/DOCS/WjWfCKUJ_P14_Jutisi-3254-ArticleText-12385-1-10-20210424.pdf");
        request.setFileType(FileType.DOCS);

        DocumentSummaryResponse mockSummaryResponse = new DocumentSummaryResponse();
        mockSummaryResponse.setData(new DocumentSummary("Sample summary"));
        when(documentService.generateSummary(any(MultipartFile.class))).thenReturn(mockSummaryResponse);

        MultipartFile mockFile = mock(MultipartFile.class);
        when(dataBucketUtil.downloadFileOrVideo(any(), any())).thenReturn(mockFile);

        // Act
        Object result = gcpUploader.upload(request, "test@example.com");

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof DocumentUploaderResponse);
        DocumentUploaderResponse response = (DocumentUploaderResponse) result;
        assertEquals("Sample summary", response.getSummary());
    }

    @Test
    @DisplayName("Upload video with success")
    public void testUpload_SuccessfulVideoFile() throws BadRequestException, UnsupportedEncodingException {
        UploadFileRequest request = new UploadFileRequest();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("video.mp4");
        request.setFile(mockFile);
        request.setFileType(FileType.VIDEO);

        FileDto mockFileDto = new FileDto("video.mp4", "http://bucket-url/video.mp4", "");
        when(dataBucketUtil.uploadFile(any(UploadFileRequest.class), anyString(), anyString())).thenReturn(mockFileDto);

        VideoTranscript mockTranscript = new VideoTranscript(null, "Sample transcript", "Sample summary", 333L);
        when(videoService.generateTranscript(any(MultipartFile.class))).thenReturn(mockTranscript);

        Object result = gcpUploader.upload(request, "test@example.com");

        assertNotNull(result);
        assertTrue(result instanceof VideoUploaderResponse);
        VideoUploaderResponse response = (VideoUploaderResponse) result;
        assertEquals("Sample transcript", response.getTranscriptData().getTranscript());
        assertEquals("http://bucket-url/video.mp4", response.getUrl());
    }

    @Test
    @DisplayName("Upload file with no name returns bad request error")
    public void testUpload_NullOriginalFilename() {
        UploadFileRequest request = new UploadFileRequest();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(null);
        request.setFile(mockFile);

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            gcpUploader.upload(request, "test@example.com");
        });

        assertEquals("Original file name is null", thrown.getMessage());
    }

    @Test
    @DisplayName("Upload non pdf doc return bad request error")
    public void testUpload_NonPDFDocument() throws BadRequestException {
        // Arrange
        UploadFileRequest request = new UploadFileRequest();
        MultipartFile mockFile = mock(MultipartFile.class);
        FileDto fileDto = new FileDto("Sample transcript", "Sample summary", "");
        when(dataBucketUtil.uploadFile(any(), any(), any())).thenReturn(fileDto);
        when(mockFile.getOriginalFilename()).thenReturn("document.txt");
        request.setFile(mockFile);
        request.setFileType(FileType.DOCS);

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            gcpUploader.upload(request, "test@example.com");
        });

        assertEquals("Error occurred while uploading: Please only provide PDF documents.", thrown.getMessage());
    }

    @Test
    @DisplayName("Delete with null id and preview video")
    public void testDeleteWithNullIdAndPreviewVideo() {
        String url = "some-url";
        String fileType = "PREVIEW_VIDEO";

        when(courseRepo.existsByThumbnailOrPreviewVideoURL(url, url)).thenReturn(true);

        gcpUploader.delete(null, url, fileType, null);

        verify(courseRepo, times(1)).existsByThumbnailOrPreviewVideoURL(url, url);
        verifyNoMoreInteractions(documentService, videoService, userCourseProgressService, dataBucketUtil);
    }

    @Test
    @DisplayName("Delete with null id and filetype docs")
    public void testDeleteWithNullIdAndDocs() {
        String url = "some-url";
        String fileType = "DOCS";

        when(documentService.existsByUrl(url)).thenReturn(true);

        gcpUploader.delete(null, url, fileType, null);

        verify(documentService, times(1)).existsByUrl(url);
        verifyNoMoreInteractions(courseRepo, videoService, userCourseProgressService, dataBucketUtil);
    }

    @Test
    @DisplayName("Delete with id and docs")
    public void testDeleteWithIdAndDocs() {
        Long id = 1L;
        String url = "some-url";
        String fileType = "DOCS";
        Long topicId = 2L;

        gcpUploader.delete(id, url, fileType, topicId);

        verify(documentService, times(1)).deleteById(id);
        verify(userCourseProgressService, times(1)).deleteAllUserCourseProgressOfVideo(topicId);
        verify(dataBucketUtil, times(1)).deleteFile(anyString());
        verifyNoMoreInteractions(courseRepo, videoService);
    }

    @Test
    @DisplayName("Delete with id and video")
    public void testDeleteWithIdAndVideo() {
        Long id = 1L;
        String url = "some-url";
        String fileType = "VIDEO";
        Long topicId = 2L;

        gcpUploader.delete(id, url, fileType, topicId);

        verify(videoService, times(1)).deleteById(id);
        verify(userCourseProgressService, times(1)).deleteAllUserCourseProgressOfVideo(topicId);
        verify(dataBucketUtil, times(1)).deleteFile(anyString());
        verifyNoMoreInteractions(courseRepo, documentService);
    }

    @Test
    @DisplayName("Delete with invalid filetype")
    public void testDeleteWithInvalidFileType() {
        Long id = 1L;
        String url = "some-url";
        String fileType = "INVALID_TYPE";
        Long topicId = 2L;

        assertThrows(IllegalArgumentException.class, () -> {
            gcpUploader.delete(id, url, fileType, topicId);
        });

        verifyNoMoreInteractions(courseRepo, documentService, videoService, userCourseProgressService, dataBucketUtil);
    }

    @Test
    @DisplayName("Delete with null id and filetype video")
    public void testDeleteWithNullIdAndVideo() {
        String url = "some-url";
        String fileType = "VIDEO";

        when(videoService.existsByUrl(url)).thenReturn(true);

        gcpUploader.delete(null, url, fileType, null);

        verify(videoService, times(1)).existsByUrl(url);
        verifyNoMoreInteractions(courseRepo, documentService, userCourseProgressService, dataBucketUtil);
    }

}
