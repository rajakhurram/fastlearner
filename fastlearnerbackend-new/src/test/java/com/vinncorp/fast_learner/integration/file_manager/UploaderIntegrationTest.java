package com.vinncorp.fast_learner.integration.file_manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.integration.util.JsonUtil;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.user.UserCourseProgress;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.repositories.docs.DocumentRepository;
import com.vinncorp.fast_learner.repositories.section.SectionRepository;
import com.vinncorp.fast_learner.repositories.topic.TopicRepository;
import com.vinncorp.fast_learner.repositories.topic.TopicTypeRepository;
import com.vinncorp.fast_learner.repositories.user.UserCourseProgressRepository;
import com.vinncorp.fast_learner.repositories.video.VideoRepository;
import com.vinncorp.fast_learner.request.uploader.ResourceDeleteRequest;
import com.vinncorp.fast_learner.response.docs.DocumentUploaderResponse;
import com.vinncorp.fast_learner.response.video.VideoUploaderResponse;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.InputStream;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UploaderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TopicRepository topicRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private SectionRepository sectionRepo;

    @Autowired
    private DocumentRepository documentRepo;

    @Autowired
    private VideoRepository videoRepo;

    @Autowired
    private TopicTypeRepository topicTypeRepo;

    @Autowired
    private UserCourseProgressRepository userCourseProgressRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${multipart.max-file-size}")
    private long MAX_FILE_SIZE;

    private String JWT_TOKEN;
    private static String uploadedDocUrl;
    private static String uploadedVideoUrl;

    @BeforeEach
    void init() throws Exception {
        JWT_TOKEN = TokenUtils.getToken(mockMvc);
    }

    @DisplayName("Test: Video File Upload - when provided valid data (Success)")
    @Test
    @Order(1)
    public void testVideoFileUpload_Success() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/video.mp4");
        try (InputStream inputStream = resource.getInputStream()) {
            MockMultipartFile mockFile = new MockMultipartFile("file", "video.mp4", "application/mp4", inputStream);

            String fileType = "VIDEO";

            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.multipart(APIUrls.UPLOADER + APIUrls.UPLOAD)
                            .file(mockFile)
                            .part(new MockPart("fileType", fileType.getBytes()))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                    .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.name()))
                    .andExpect(jsonPath("$.message").value("Saved the file successfully."))
                    .andExpect(jsonPath("$.data").exists());

            MvcResult mvcResult = result.andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            String videoUploaderResponse = jsonNode.get("data").toString();
            VideoUploaderResponse response = objectMapper.readValue(videoUploaderResponse, VideoUploaderResponse.class);
            uploadedVideoUrl = response.getUrl();

            System.out.println("VIDEO URL: " + uploadedVideoUrl);
        }
    }

    @DisplayName("Test: Document File Upload - when provided valid data (Success)")
    @Test
    @Order(2)
    public void testDocumentFileUpload_Success() throws Exception {
        System.out.println("-------------------------------VIDEO URL: " + uploadedVideoUrl);
        ClassPathResource resource = new ClassPathResource("static/doc.pdf");
        try (InputStream inputStream = resource.getInputStream()) {
            MockMultipartFile mockFile = new MockMultipartFile("file", "doc.pdf", "application/pdf", inputStream);

            String fileType = "DOCS";

            ResultActions result = mockMvc.perform(multipart(APIUrls.UPLOADER + APIUrls.UPLOAD)
                            .file(mockFile)
                            .part(new MockPart("fileType", fileType.getBytes()))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                    .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.name()))
                    .andExpect(jsonPath("$.message").value("Saved the file successfully."))
                    .andExpect(jsonPath("$.data").exists());

            MvcResult mvcResult = result.andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            String videoUploaderResponse = jsonNode.get("data").toString();
            DocumentUploaderResponse response = objectMapper.readValue(videoUploaderResponse, DocumentUploaderResponse.class);
            uploadedDocUrl = response.getUrl();
            System.out.println("DOC URL: " + uploadedDocUrl);
        }
    }

    @DisplayName("Test: File Upload - when provided invalid data")
    @Test
    @Order(3)
    public void testFileUploadWithInvalidFileType() throws Exception {
        System.out.println("-------------------------------DOC URL: " + uploadedDocUrl);
        ClassPathResource resource = new ClassPathResource("static/audio.mp3");
        try (InputStream inputStream = resource.getInputStream()) {
            MockMultipartFile mockFile = new MockMultipartFile("file", "audio.mp3", "application/mp3", inputStream);

            String fileType = "MP3";

            mockMvc.perform(multipart(APIUrls.UPLOADER + APIUrls.UPLOAD)
                            .file(mockFile)
                            .part(new MockPart("fileType", fileType.getBytes()))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("File type is not valid."));
        }
    }

    @DisplayName("Test: File Upload - when provided invalid file")
    @Test
    @Order(4)
    public void testFileUploadWithoutFile() throws Exception {
        String fileType = "MP3";
        mockMvc.perform(multipart(APIUrls.UPLOADER + APIUrls.UPLOAD)
                        .part(new MockPart("fileType", fileType.getBytes()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please upload a valid file."));
    }

    @DisplayName("Test: Upload For Regeneration - when provided valid file (Success)")
    @Test
    @Order(5)
    public void testUploadForRegeneration() throws Exception {
        // Perform the file upload request
        mockMvc.perform(multipart(APIUrls.UPLOADER + APIUrls.UPLOAD_FOR_REGENERATION)
                        .param("fileType", "DOCS")
                        .param("url", uploadedDocUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.name()))
                .andExpect(jsonPath("$.message").value("Saved the file successfully."))
                .andExpect(jsonPath("$.data").exists());
    }

    @DisplayName("Test: Upload For Regeneration - when provided invalid file type")
    @Test
    @Order(6)
    public void testUploadForRegenerationWithInvalidFileType() throws Exception {
        // Perform the file upload request with an invalid file type
        mockMvc.perform(multipart(APIUrls.UPLOADER + APIUrls.UPLOAD_FOR_REGENERATION)
                        .param("fileType", "INVALID_TYPE") // Invalid file type
                        .param("url", "http://example.com/file-to-regenerate.txt") // Replace with an actual valid URL
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File type is not valid."));
    }

    @DisplayName("Test: Upload For Regeneration - when url is not provided")
    @Test
    @Order(7)
    public void testUploadForRegenerationWithoutUrl() throws Exception {
        // Perform the file upload request without a URL
        mockMvc.perform(multipart(APIUrls.UPLOADER + APIUrls.UPLOAD_FOR_REGENERATION)
                        .param("fileType", "TEXT") // Replace with an actual valid file type enum value
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Required parameter 'url' is not present."));
    }

    @DisplayName("Test: Upload For Regeneration - when provided invalid url")
    @Test
    @Order(8)
    public void testUploadForRegenerationWithInvalidUrl() throws Exception {
        // Perform the file upload request with an invalid URL
        mockMvc.perform(multipart(APIUrls.UPLOADER + APIUrls.UPLOAD_FOR_REGENERATION)
                        .param("fileType", "DOCS") // Replace with an actual valid file type enum value
                        .param("url", "invalid-url") // Invalid URL
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please only provide pdf document."));
    }

    @DisplayName("Test: Delete Resource - when provided valid data (Success)")
    @Test
    @Order(9)
    public void deleteResource_shouldReturnOk_whenRequestIsValid() throws Exception {
        Course course = courseRepo.findById(43L).get();
        Section section = sectionRepo.findById(345L).orElseThrow(() -> new EntityNotFoundException("No section found."));

        Topic topic = Topic.builder()
                .section(section)
                .durationInSec(333)
                .delete(false)
                .creationDate(new Date())
                .topicType(topicTypeRepo.findById(1L).get())
                .name("Test Topic")
                .sequenceNumber(444)
                .build();
        topic = topicRepo.save(topic);

        Video video = Video.builder()
                .topic(topic)
                .uploadedDate(new Date())
                .videoURL(uploadedVideoUrl)
                .vttContent(null)
                .summary("Test Summary")
                .filename("video file")
                .delete(false)
                .transcribe("Test Transcript")
                .build();
        video = videoRepo.save(video);

        UserCourseProgress userCourseProgress = new UserCourseProgress();
        userCourseProgress.setCourse(course);
        userCourseProgress.setSection(section);
        userCourseProgress.setTopic(topic);
        userCourseProgress.setCompleted(false);
        userCourseProgress.setSeekTime(222L);
        userCourseProgress.setCreationDate(new Date());

        userCourseProgress = userCourseProgressRepo.save(userCourseProgress);

        ResourceDeleteRequest request = ResourceDeleteRequest.builder()
                .id(video.getId())
                .url(uploadedVideoUrl)
                .fileType("VIDEO")
                .topicId(topic.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.delete(APIUrls.UPLOADER + APIUrls.DELETE_RESOURCE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                        .content(JsonUtil.asJsonString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        topicRepo.deleteById(topic.getId());
    }
}
