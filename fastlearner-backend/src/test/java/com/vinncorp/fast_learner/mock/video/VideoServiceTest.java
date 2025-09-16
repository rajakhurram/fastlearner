package com.vinncorp.fast_learner.mock.video;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.video.VideoRepository;
import com.vinncorp.fast_learner.dtos.video.VideoTranscript;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.response.video.VideoTranscriptResponse;
import com.vinncorp.fast_learner.services.video.VideoService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.TimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VideoServiceTest {

    @InjectMocks
    private VideoService videoService;

    @Mock
    private VideoRepository repo;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TimeUtil timeUtil;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(videoService, "TRANSCRIPT_GENERATION_URL", "http://localhost:5000/api/v1/generate-transcript");
        ReflectionTestUtils.setField(videoService, "YOUTUBE_VIDEO_DOWNLOADER", "http://localhost:5000/api/v1/download-video");
        ReflectionTestUtils.setField(videoService, "TRANSCRIPT_GENERATION_AUTH_KEY", "ew4t34vt32tvt43v");
        ReflectionTestUtils.setField(videoService, "YOUTUBE_API_KEY", "4tv2twvt4wveegsev");
        ReflectionTestUtils.setField(videoService, "YOUTUBE_DURATION_URL", "sfoiejij8439ii54jtor");
    }

    @Test
    @DisplayName("Test: Get video by topic ID - success")
    public void testGetVideoByTopicId_Success() throws EntityNotFoundException, IOException {
        Video expectedVideo = VideoTestData.video();
        when(repo.findByTopicId(1L)).thenReturn(Optional.of(expectedVideo));

        Video actualVideo = videoService.getVideoByTopicId(1L);

        assertEquals(expectedVideo, actualVideo);
        verify(repo, times(1)).findByTopicId(1L);
    }

    @Test
    @DisplayName("Test: Get video by topic ID - not found")
    public void testGetVideoByTopicId_NotFound() {
        when(repo.findByTopicId(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> videoService.getVideoByTopicId(1L));
        verify(repo, times(1)).findByTopicId(1L);
    }

    @Test
    @DisplayName("Test: Save video - success")
    public void testSaveVideo_Success() throws InternalServerException, IOException {
        Video video = VideoTestData.video();
        when(repo.save(video)).thenReturn(video);

        Video savedVideo = videoService.save(video);

        assertEquals(video, savedVideo);
        verify(repo, times(1)).save(video);
    }

    @Test
    @DisplayName("Test: Save video - delete video")
    public void testSaveVideo_Delete() throws InternalServerException, IOException {
        Video video = VideoTestData.video();
        video.setDelete(true);

        Video savedVideo = videoService.save(video);

        assertNull(savedVideo);
        verify(repo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Test: Save video - failure")
    public void testSaveVideo_Failure() {
        Video video = new Video();
        when(repo.save(video)).thenThrow(new RuntimeException("Error"));

        assertThrows(InternalServerException.class, () -> videoService.save(video));
        verify(repo, times(1)).save(video);
    }

    @Test
    @DisplayName("Test: Generate transcript - success")
    public void testGenerateTranscript_Success() throws BadRequestException {
        MultipartFile file = mock(MultipartFile.class);
        VideoTranscriptResponse mockResponse = VideoTestData.videoTranscriptResponse();

        // Use MockedConstruction to mock the creation of RestTemplate when it is instantiated in the method
        try (MockedConstruction<RestTemplate> mockedRestTemplate = Mockito.mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    // Mock the getForObject method for this instance of RestTemplate
                    doReturn(ResponseEntity.of(Optional.of(mockResponse))).when(mock).postForEntity(anyString(), any(HttpEntity.class), eq(VideoTranscriptResponse.class));
                })) {

            VideoTranscript transcript = videoService.generateTranscript(file);

            assertNotNull(transcript);
        }
    }

    @Test
    @DisplayName("Test: Generate transcript - failure")
    public void testGenerateTranscript_Failure() {
        MultipartFile file = mock(MultipartFile.class);
        try (MockedConstruction<RestTemplate> mockedRestTemplate = Mockito.mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    // Mock the getForObject method for this instance of RestTemplate
                    doThrow(new RuntimeException("Error")).when(mock).postForEntity(anyString(), any(HttpEntity.class), eq(VideoTranscriptResponse.class));
                })) {
            assertThrows(BadRequestException.class, () -> videoService.generateTranscript(file));
        }
    }

    @Test
    @DisplayName("Test: Fetch duration - success")
    public void testFetchDuration_Success() {
        String videoId = "video123";
        Map<String, Object> mockResponseBody = new HashMap<>();
        Map<String, Object> contentDetails = new HashMap<>();
        contentDetails.put("duration", "PT2M30S");

        Map<String, Object> firstItem = new HashMap<>();
        firstItem.put("contentDetails", contentDetails);

        List<Map<String, Object>> items = new ArrayList<>();
        items.add(firstItem);

        mockResponseBody.put("items", items);
        try (MockedConstruction<RestTemplate> mockedRestTemplate = Mockito.mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    // Mock the getForObject method for this instance of RestTemplate
                    doReturn(new ResponseEntity<>(mockResponseBody, HttpStatus.OK)).when(mock).getForEntity(anyString(), eq(Map.class));
                })) {
            /*when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                    .thenReturn(new ResponseEntity<>(mockResponseBody, HttpStatus.OK));*/
            Message<Integer> response = videoService.fetchDuration(videoId, "test@example.com");

            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertEquals(150, response.getData());
        }
    }

    @Test
    @DisplayName("Test: Fetch duration - invalid video ID")
    public void testFetchDuration_InvalidVideoId() {
        String videoId = "invalidVideoId";
        HashMap<String, String> d = new HashMap<>();
        try (MockedConstruction<RestTemplate> mockedRestTemplate = Mockito.mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    // Mock the getForObject method for this instance of RestTemplate
                    doReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST)).when(mock).getForEntity(anyString(), eq(Map.class));
                })) {
            Message<Integer> response = videoService.fetchDuration(videoId, "test@example.com");

            assertNotNull(response);
            assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
            assertEquals(response.getCode(), HttpStatus.BAD_REQUEST.name());
            assertEquals(response.getMessage(), "Maybe the video id is not valid please provide valid id.");
            assertNull(response.getData());
        }
    }
}
