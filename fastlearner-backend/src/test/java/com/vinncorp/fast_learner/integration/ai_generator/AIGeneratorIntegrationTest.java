package com.vinncorp.fast_learner.integration.ai_generator;

import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AIGeneratorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String JWT_TOKEN;

    @BeforeEach
    void init() throws Exception {

        if(JWT_TOKEN == null)
            JWT_TOKEN = TokenUtils.getToken(mockMvc);
    }

    @DisplayName("Test: Generate Topic Or Article - With valid data (Success)")
    @Test
    public void generateTopicOrArticle_whenValidInput_thenReturnsTopicOrArticle() throws Exception {
        String input = "Write a topic on machine learning.";
        mockMvc.perform(MockMvcRequestBuilders.post(APIUrls.AI_GENERATOR + APIUrls.GET_TOPIC_OR_ARTICLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .param("input", input))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @DisplayName("Test: Generate Keywords - With course title")
    @Test
    public void testGenerateKeywords_withCourseTitle() throws Exception {
        mockMvc.perform(post(APIUrls.AI_GENERATOR + APIUrls.GET_KEYWORDS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .content("{\"courseTitle\":\"Sample Course\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.name()))
                .andExpect(jsonPath("$.message").value("Keywords Generated successfully"))
                .andExpect(jsonPath("$.data[0]").exists());
    }

    @DisplayName("Test: Generate Keywords - With section and topic names")
    @Test
    public void testGenerateKeywords_withSectionAndTopicNames() throws Exception {
        mockMvc.perform(post(APIUrls.AI_GENERATOR + APIUrls.GET_KEYWORDS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .content("{\"sectionTitle\":\"Sample Section\",\"topicNames\":[\"Topic1\",\"Topic2\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.name()))
                .andExpect(jsonPath("$.message").value("Keywords Generated successfully"))
                .andExpect(jsonPath("$.data[0]").exists());
    }

    @DisplayName("Test: Regenerate Summary - With video file (Success)")
    @Test
    public void testRegenerateSummary_withVideoFile() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/video.mp4");
        try (InputStream inputStream = resource.getInputStream()) {
            MockMultipartFile mockFile = new MockMultipartFile("file", "video.mp4", "video/mp4", inputStream);

            String fileType = "VIDEO";

            mockMvc.perform(multipart(APIUrls.AI_GENERATOR + APIUrls.REGENERATE_SUMMARY)
                            .file(mockFile)
                            .part(new MockPart("fileType", fileType.getBytes()))
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.code").value(HttpStatus.OK.name()))
                    .andExpect(jsonPath("$.message").value("Regenerated the video summary."))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @DisplayName("Test: Regenerate Summary - With pdf file (Success)")
    @Test
    public void testRegenerateSummary_withDocumentFile() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/doc.pdf");
        try (InputStream inputStream = resource.getInputStream()) {
            MockMultipartFile mockFile = new MockMultipartFile("file", "doc.pdf", "application/pdf", inputStream);

            String fileType = "DOCS";

            mockMvc.perform(multipart(APIUrls.AI_GENERATOR + APIUrls.REGENERATE_SUMMARY)
                            .file(mockFile)
                            .part(new MockPart("fileType", fileType.getBytes()))
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.code").value(HttpStatus.OK.name()))
                    .andExpect(jsonPath("$.message").value("Regenerated the document summary."))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @DisplayName("Test: Regenerate Summary - With audio file (Bad Request)")
    @Test
    public void testRegenerateSummary_withUnsupportedFileType() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/audio.mp3");
        try (InputStream inputStream = resource.getInputStream()) {
            MockMultipartFile mockFile = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", inputStream);

            String fileType = "AUDIO";

            mockMvc.perform(multipart(APIUrls.AI_GENERATOR + APIUrls.REGENERATE_SUMMARY)
                            .file(mockFile)
                            .part(new MockPart("fileType", fileType.getBytes()))
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Provided file type is not supported."));
        }
    }
}
