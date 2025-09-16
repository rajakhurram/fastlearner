package com.vinncorp.fast_learner.integration.youtubeDownloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.services.video.IVideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class YoutubeDownloaderIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private IVideoService videoService;

    private String jwtToken;

    private TokenUtils tokenUtils;
    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Should successfully fetch video duration")
    public void testFetchDuration_Success() throws Exception {
        String validVideoId = "dQw4w9WgXcQ"; // Example valid video ID

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/youtube-video/duration")
                        .param("videoId", validVideoId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should Return 400 Bad Request")
    public void testFetchDuration_BadRequest() throws Exception {
        String validVideoId = "22"; // Example valid video ID

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/youtube-video/duration")
                        .param("videoId", validVideoId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized if no authorization token is provided")
    public void testFetchDuration_MissingAuthToken() throws Exception {
        String validVideoId = "dQw4w9WgXcQ"; // Example valid video ID

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/youtube-video-duration")
                        .param("videoId", validVideoId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when no video ID is provided")
    public void testFetchDuration_NoVideoId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/youtube-video/duration")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
