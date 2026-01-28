package com.vinncorp.fast_learner.integration.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.integration.user.UserIntegrationTestData;
import com.vinncorp.fast_learner.models.notification.Notification;
import com.vinncorp.fast_learner.repositories.notification.NotificationRepository;
import com.vinncorp.fast_learner.request.notification.NotificationDeleteRequest;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Constants.NotificationConstant;
import com.vinncorp.fast_learner.test_util.Constants;
import com.vinncorp.fast_learner.util.enums.NotificationContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;
import java.util.List;

import static com.vinncorp.fast_learner.util.Constants.APIUrls.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private NotificationRepository notificationRepository;
    private String jwtToken;
    private String timeStamp = "123456789";
    private static final String NOTIFICATION = APIUrls.NOTIFICATION;

    @BeforeEach
    public void setup() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Successfully register and fetch notifications")
    public void register_whenValidUser_thenReturnsNotifications() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(NOTIFICATION + FETCH_NOTIFICATION.replace("{timestamp}", this.timeStamp))
                        .param("token", jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Fetch all notifications with pagination successfully")
    public void fetchAllNotifications_whenValidRequest_thenReturnsNotifications() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(NOTIFICATION + FETCH_ALL_NOTIFICATION_WITH_PAGINATION)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .param("pageNo", "0")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Fetching notification is successful."));
    }

    @Test
    @DisplayName("Return 400 Bad Request when page number or page size is negative")
    public void fetchAllNotifications_whenNegativePageNoOrPageSize_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(NOTIFICATION + FETCH_ALL_NOTIFICATION_WITH_PAGINATION)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .param("pageNo", "-1")
                        .param("pageSize", "2"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(MockMvcRequestBuilders.get(NOTIFICATION + FETCH_ALL_NOTIFICATION_WITH_PAGINATION)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .param("pageNo", "0")
                        .param("pageSize", "-2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Successfully delete notifications")
    public void deleteNotification_whenValidRequest_thenReturnsOk() throws Exception {
        Notification notification = Notification.builder()
//                .content(NotificationConstant.value(UserIntegrationTestData.userData().getFullName(), "any", NotificationType.COURSE_REVIEW))
                .contentType(NotificationContentType.TEXT)
                .type(NotificationType.COURSE_REVIEW)
                .senderName(UserIntegrationTestData.userData().getFullName())
                .senderImageURL("any")
                .receiverIds(List.of(UserIntegrationTestData.userData().getId()))
                .url("any")
                .creationDate(new Date())
                .isRead(false)
                .build();
        Notification savedNotification = this.notificationRepository.save(notification);

        NotificationDeleteRequest deleteRequest = new NotificationDeleteRequest();
        deleteRequest.setNotificationId(List.of(savedNotification.getId()));
        mockMvc.perform(MockMvcRequestBuilders.delete(NOTIFICATION + DELETE_NOTIFICATION)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(deleteRequest)));
    }

}

