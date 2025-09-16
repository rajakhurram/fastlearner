package com.vinncorp.fast_learner.integration.topic;

import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.test_util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.vinncorp.fast_learner.util.Constants.APIUrls.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class TopicTypeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    private String jwtToken;
    private static final String TOPIC_TYPE_API = APIUrls.TOPIC_TYPE_API;

    @BeforeEach
    public void setup() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Fetch all topic types successfully")
    public void fetchAllTopicType_whenDataExists_thenReturnsSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TOPIC_TYPE_API + GET_ALL_TOPIC_TYPE)
                .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("All topic type is fetched successfully."));
    }
}
