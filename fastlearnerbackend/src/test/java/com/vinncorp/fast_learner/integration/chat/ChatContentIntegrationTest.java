package com.vinncorp.fast_learner.integration.chat;

import com.vinncorp.fast_learner.integration.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatContentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    private String jwtToken;

    private TokenUtils tokenUtils;
    //this test will work once python is active.
//    @Test
//    public void testFetchChatContentSuccess() throws Exception {
//        Long courseId = 45L;
//        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzczMzMzNSwiaWF0IjoxNzIzNzIxMzM1LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.PtqZTA1YbP3U6qO3OL4NyywotODZlM5cH9ICpkdogsuRMImbApt17JMGOltKIH-3y9hiNoZBzauIxbnAFQLKrA";
//        mockMvc.perform(get("/api/v1/chat/")
//                        .param("courseId", String.valueOf(courseId))
//                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.message").value("Fetching all chat contents for logged in user."));
//    }

    @BeforeEach
    public void setup() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);

    }
    @Test
    public void testFetchChatContentNotFound() throws Exception {
        Long courseId = 45L;
        //String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzczMzMzNSwiaWF0IjoxNzIzNzIxMzM1LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.PtqZTA1YbP3U6qO3OL4NyywotODZlM5cH9ICpkdogsuRMImbApt17JMGOltKIH-3y9hiNoZBzauIxbnAFQLKrA";
        mockMvc.perform(get("/api/v1/chat/")
                        .param("courseId", String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    public void testFetchChatContentInvalidToken() throws Exception {
        Long courseId = 45L;
        String token = "eyJhbGciOiJIUzUx2MiJ239.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzczMzMzNSwiaWF0IjoxNzIzNzIxMzM1LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.PtqZTA1YbP3U6qO3OL4NyywotODZlM5cH9ICpkdogsuRMImbApt17JMGOltKIH-3y9hiNoZBzauIxbnAFQLKrA";


        mockMvc.perform(get("/api/v1/chat/")
                        .param("courseId", String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testFetchChatContentNotEnrolled() throws Exception {
        Long courseId = 28L; // Assuming this courseId is valid but the user is not enrolled
      //  String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzczMzMzNSwiaWF0IjoxNzIzNzIxMzM1LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.PtqZTA1YbP3U6qO3OL4NyywotODZlM5cH9ICpkdogsuRMImbApt17JMGOltKIH-3y9hiNoZBzauIxbnAFQLKrA";

        mockMvc.perform(get("/api/v1/chat/")
                        .param("courseId", String.valueOf(courseId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

//    @Test
//    @DisplayName("Should successfully create a chat")
//    public void testCreateChat_Success() throws Exception {
//        CreateChatRequest request = new CreateChatRequest();
//        request.setCourseId(29L); // Ensure this course ID exists in the test database
//        request.setTopicId(85L);  // Ensure this topic ID exists in the test database
//        request.setQuestion("What is the topic about?");
//
//        request.setTime("2024-08-23T10:00:00Z");
//        mockMvc.perform(post("/api/v1/chat/")
//                        .header("Authorization", "Bearer " + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(new ObjectMapper().writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Chat service responded successfully."))
//                .andExpect(jsonPath("$.data.answer").isNotEmpty());
//    }

//    @Test
//    @DisplayName("Should successfully retrieve chat history")
//    public void testGetChatHistory_Success() throws Exception {
//        Long validChatId = 80L;
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/chat-history/" + validChatId)
//                        .header("Authorization", "Bearer " + jwtToken)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());// Adjust based on actual response structure
//    }

    @Test
    @DisplayName("Should return 404 Not Found when no chat history exists for the chat ID")
    public void testGetChatHistory_ChatIdNotFound() throws Exception {
        Long invalidChatId = 9999L;
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/chat-history/" + invalidChatId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
