package com.vinncorp.fast_learner.integration.tag;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
public class TagIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

//    @Test
//
//    public void testGetAllTagByNameSuccess() throws Exception {
//        String name = "test"; // Adjust this to match your test data
//        mockMvc.perform(get("/api/v1/tag/")
//                        .param("name", name)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is(200)))
//                .andExpect(jsonPath("$.message", is("Successfully fetched all tags.")))
//                .andExpect(jsonPath("$.data", hasSize(2))) // Adjust based on expected size
//                .andExpect(jsonPath("$.data[0].name", is("exampleTag1"))) // Adjust based on expected data
//                .andExpect(jsonPath("$.data[1].name", is("exampleTag2"))); // Adjust based on expected data
//    }
//
//    @Test
//    public void testGetAllTagByNameNoTagsFound() throws Exception {
//        String name = "nonexistent"; // Ensure this name is not present in your test database
//
//        // Perform the request
//        mockMvc.perform(get("/api/v1/tags")
//                        .param("name", name)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status", is(404)))
//                .andExpect(jsonPath("$.message", is("No tags found.")))
//                .andExpect(jsonPath("$.data").doesNotExist()); // Ensure no data is present
//    }
}
