package com.vinncorp.fast_learner.integration.payment.checkout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.dtos.payment.checkout.ChargePayment;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
import com.vinncorp.fast_learner.repositories.enrollment.EnrollmentRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.enums.CourseType;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequiredArgsConstructor
public class PaymentCheckoutIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositories to setup data
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private final Long COURSE_ID = 9999L;

    @BeforeEach
    void setupData() {

        userRepository.deleteById(COURSE_ID);
        // Setup test user
        User user = new User();
        user.setId(9999L);
        user.setEmail("testuser@example.com");
        user.setFullName("Test User");
        userRepository.save(user);

        courseRepository.deleteById(COURSE_ID);
        // Setup test course
        Course course = new Course();
        course.setId(9999L);
        course.setTitle("Test Premium Course");
        course.setPrice(100.0);
        course.setCourseType(CourseType.PREMIUM_COURSE);
        courseRepository.save(course);
    }

    @Test
    void testChargePayment_withValidData_shouldSucceed() throws Exception {
        ChargePayment chargePayment = new ChargePayment();
        chargePayment.setCourseId(COURSE_ID);
        chargePayment.setOpaqueData("fake-token"); // simulate a valid token for testing
        chargePayment.setCoupon(null);

        mockMvc.perform(post(APIUrls.COURSE_CHECKOUT_API + APIUrls.CHARGE_PAYMENT)
                        .principal(() -> TokenUtils.EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chargePayment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("Successfully charged")));
    }

    @Test
    void testFetchAllPayoutHistory_shouldReturnEmptyInitially() throws Exception {
        mockMvc.perform(post("/api/v1/course/checkout/get-all-payout-history")
                        .principal(() -> "testuser@example.com")
                        .param("pageNo", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fetched payout history")) // if this is the default message
                .andExpect(jsonPath("$.data").exists());
    }
}