package com.vinncorp.fast_learner.mock.file_manager.uploader;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.vinncorp.fast_learner.dtos.file_manager.uploader.gcp.FileDto;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.request.uploader.UploadFileRequest;
import com.vinncorp.fast_learner.util.enums.FileType;
import com.vinncorp.fast_learner.util.gcp.DataBucketUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DataBucketUtilTest {
    private Storage storage;
    private Bucket bucket;

    @Mock
    private Blob blob;

    @InjectMocks
    private DataBucketUtil dataBucketUtil;

    private static final String GCP_BUCKET_NAME = "my-bucket";
    private static final String GCP_BUCKET_URL = "https://storage.googleapis.com/my-bucket/";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // Set the GCP_BUCKET_NAME and GCP_BUCKET_URL fields
        ReflectionTestUtils.setField(dataBucketUtil, "GCP_BUCKET_NAME", GCP_BUCKET_NAME);
        ReflectionTestUtils.setField(dataBucketUtil, "GCP_BUCKET_URL", GCP_BUCKET_URL);
    }

    /*@Test
    public void testUploadFile_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello World".getBytes());
        UploadFileRequest request = new UploadFileRequest(file, FileType.DOCS, null);
        String fileName = "test.txt";
        String contentType = "text/plain";

        when(storage.get(anyString(), any(Storage.BucketGetOption.class))).thenReturn(bucket);
        when(bucket.create(anyString(), any(byte[].class), anyString())).thenReturn(blob);

        // Act
        FileDto result = dataBucketUtil.uploadFile(request, fileName, contentType);

        // Assert
        assertEquals(fileName, result.getFileName());
        assertEquals(GCP_BUCKET_URL + "DOCS/" + fileName, result.getFileUrl());
        verify(storage).get(anyString(), any(Storage.BucketGetOption.class));
        verify(bucket).create(anyString(), any(byte[].class), anyString());
    }
*/
    @Test
    public void testUploadFile_ThrowsBadRequestException_WhenFileDataCannotBeRetrieved() throws Exception {
        // Arrange
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("Cannot read bytes"));
        UploadFileRequest request = new UploadFileRequest(file, FileType.DOCS, null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> dataBucketUtil.uploadFile(request, "test.txt", "text/plain"));
    }

    /*@Test
    public void testUploadFile_ThrowsBadRequestException_WhenBlobIsNull() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello World".getBytes());
        UploadFileRequest request = new UploadFileRequest(file, FileType.DOCS, null);

        when(storage.get(anyString(), any(Storage.BucketGetOption.class))).thenReturn(bucket);
        when(bucket.create(anyString(), any(byte[].class), anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> dataBucketUtil.uploadFile(request, "test.txt", "text/plain"));
    }

    @Test
    public void testUploadFile_ThrowsBadRequestException_OnExceptionDuringUpload() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello World".getBytes());
        UploadFileRequest request = new UploadFileRequest(file, FileType.DOCS, null);

        when(storage.get(anyString(), any(Storage.BucketGetOption.class))).thenReturn(bucket);
        when(bucket.create(anyString(), any(byte[].class), anyString())).thenThrow(new RuntimeException("GCS Error"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> dataBucketUtil.uploadFile(request, "test.txt", "text/plain"));
    }*/
}