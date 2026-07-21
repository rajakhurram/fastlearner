package com.vinncorp.fast_learner.integration.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    private String jwtToken;

    private TokenUtils tokenUtils;
    @BeforeEach
    public void setUp() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);

    }

    @Test
    @DisplayName("Fetch Dashboard Stats Successfully")
    public void fetchStats_whenValidRequest_thenReturnsStats() throws Exception {
        String filterBy = "Monthly";
        String url = "/api/v1/dashboard/stats?filterBy=" + filterBy;
       // String jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzQ2ODI4NCwiaWF0IjoxNzIzNDU2Mjg0LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.z_fS_wF3crMVVWRAFqEe6qFoQJtslHcnDOAS6KCUhqxWaJDClxN0JukFjaV-USiPCACTtTFb4FKnpc0OVBb7vg";

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Fetch Dashboard Stats Successfully")
    public void fetchStats_JWTInvalid_thenReturnsStats() throws Exception {
        String filterBy = "Monthly";
        String url = "/api/v1/dashboard/stats?filterBy=" + filterBy;
        String jwtToken = "eyJhbGciOiJIUzUxMiJ1231239.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzQ1ODQzNywiaWF0IjoxNzIzNDQ5NDM3LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.Fknuw90DBqq1fAOiSFUY0TOvAmBQ2tUD-2Xb7bfUHzU-AR7XQfqEr-C0AqXJA0oJ5zja1wbj10OzI0T3NKgG0g";

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("Fetch Dashboard Stats with Invalid Filter")
    public void fetchStats_whenInvalidFilter_thenReturnsBadRequest() throws Exception {
        String filterBy = "InvalidFilter"; // Invalid filter value
        String url = "/api/v1/dashboard/stats?filterBy=" + filterBy;
       // String jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbnN0cnVjdG9yMUBtYWlsaW5hdG9yLmNvbSIsImV4cCI6MTcyMzQ2ODI4NCwiaWF0IjoxNzIzNDU2Mjg0LCJJTlNUUlVDVE9SIjoiSU5TVFJVQ1RPUiJ9.z_fS_wF3crMVVWRAFqEe6qFoQJtslHcnDOAS6KCUhqxWaJDClxN0JukFjaV-USiPCACTtTFb4FKnpc0OVBb7vg";

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Filter should be This month, Last month, This year or Last year only."));
    }
}