package com.vinncorp.fast_learner.integration.certificateController;


import com.vinncorp.fast_learner.config.LiquibaseRunner;
import com.vinncorp.fast_learner.integration.TokenUtils;
import com.vinncorp.fast_learner.integration.user.UserIntegrationTestData;
import com.vinncorp.fast_learner.models.certificate.Certificate;
import com.vinncorp.fast_learner.repositories.certificate.CertificateRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.services.certificate.CertificateService;
import com.vinncorp.fast_learner.test_util.Constants;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.vinncorp.fast_learner.util.Constants.APIUrls.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class CertificateControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private String jwtToken;
    String validCertificateId  = "2e838fd1-c954-4cb2-bad9-beeeb99a55cd";
    String invalidCertificateId = "2e838213fd1-c954-4cb2-bad9-beeeb99a55cd";
    private TokenUtils tokenUtils;
    @Autowired
    private CertificateService certificateService;
    private Certificate certificate;
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private UserRepository userRepository;
    private static String CERTIFICATE = APIUrls.CERTIFICATE;

    @BeforeEach
    public void setup() throws Exception {
        jwtToken = TokenUtils.getToken(mockMvc);
    }

    @Test
    @DisplayName("Download Certificate Successfully")
    public void downloadCertificate_whenValidCourseAndUser_thenReturnsCertificateImage() throws Exception {
        mockMvc.perform(get(CERTIFICATE + DOWNLOAD_CERTIFICATE)
                        .param("courseId", Constants.VALID_COURSE_ID.toString())
                        .param("isDownloadable", String.valueOf(true))
                        .param("token", jwtToken)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Return 400 Bad Request When Course ID is Invalid")
    public void downloadCertificate_whenInvalidCourseId_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(get(CERTIFICATE + DOWNLOAD_CERTIFICATE)
                        .param("courseId", String.valueOf(Constants.INVALID_COURSE_ID))
                        .param("isDownloadable", String.valueOf(false))
                        .param("token", jwtToken)
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("The course has not provided a certificate for students."));
    }

    @Test
    @DisplayName("Verify to Certificate Successfully")
    public void verifyCertificateForResponse_whenValidUUID_thenReturnsSuccessMessage() throws Exception {
        var m = certificateService.generateCertificate(Constants.VALID_COURSE_ID, UserIntegrationTestData.userData().getEmail());
        mockMvc.perform(get(CERTIFICATE + VERIFY_CERTIFICATE_FOR_RESPONSE.replace("{certificateId}", m.getData().getUuid()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Certificate is verified successfully."))
                .andExpect(jsonPath("$.data").value("Certificate is verified successfully."));
    }

    @Test
    @DisplayName("Return 400 Bad Request When Certificate to UUID is Invalid")
    public void verifyCertificateForResponse_whenInvalidUUID_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(get(CERTIFICATE + VERIFY_CERTIFICATE_FOR_RESPONSE.replace("{certificateId}", Constants.INVALID_CERTIFICATE_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Certificate is not valid."));
    }

    @Test
    @DisplayName("Return 400 Bad Request When UUID ID is Missing")
    public void verifyCertificateForResponse_whenCertificateIdIsMissing_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(get(CERTIFICATE + VERIFY_CERTIFICATE_FOR_RESPONSE.replace("{certificateId}",""))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Generate Certificate Successfully")
    public void generateCertificate_whenValidCourseAndUser_thenReturnsCertificateData() throws Exception {
        mockMvc.perform(get(CERTIFICATE + GET_CERTIFICATE.replace("{courseId}", Constants.VALID_COURSE_ID.toString()))
                        .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Certificate data is successfully fetched."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists());
    }

    @DisplayName("Fail to Generate Certificate when Course Doesn't Provide Certificate")
    @Test
    public void generateCertificate_whenCourseNotProvidingCertificate_thenBadRequest() throws Exception {
        Long courseWithoutCertificateId = 6L; // Replace with a courseId that doesn't provide a certificate
        String url = "/api/v1/certificate/generate/" + courseWithoutCertificateId;
        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken) // Set the Authorization header
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("The course has not provided a certificate for students."));
    }

    @DisplayName("Fail to Generate Certificate when Course Progress is Insufficient")
    @Test
    public void generateCertificate_whenCourseProgressInsufficient_thenBadRequest() throws Exception {
        Long courseWithInsufficientProgressId = 28L;
        String url = "/api/v1/certificate/generate/" + courseWithInsufficientProgressId;
        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken) // Set the Authorization header
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("The course progress should be greater than 70% for generating certificate."));
    }

    @DisplayName("Fail to Generate Certificate when Course or User Not Found")
    @Test
    public void generateCertificate_whenCourseOrUserNotFound_thenEntityNotFound() throws Exception {
        Long invalidCourseId = 9199L; // Replace with a non-existing courseId
        String url = "/api/v1/certificate/generate/" + invalidCourseId;
        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken) // Set the Authorization header
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("The course has not provided a certificate for students."));
    }

    @Test
    @DisplayName("Verify Certificate Successfully")
    public void verifyCertificate_whenValidCertificateId_thenReturnsCertificateImage() throws Exception {
        String certificateId = "2e838fd1-c954-4cb2-bad9-beeeb99a55cd";
        String url = "/api/v1/certificate/verify/" + certificateId;
        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE))
                .andExpect(result -> {

                    byte[] responseBody = result.getResponse().getContentAsByteArray();
                    assertThat(responseBody).isNotEmpty();
                });
    }

    @Test
    @DisplayName("Verify Certificate with Invalid Certificate ID")
    public void verifyCertificate_whenInvalidCertificateId_thenReturnsNotFound() throws Exception {
        String invalidCertificateId = "invalid-uuid";
        String url = "/api/v1/certificate/verify/" + invalidCertificateId;
        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Expecting 404 Not Found
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Certificate not verified with provided uuid."));
    }

    @Test
    @DisplayName("Verify Certificate with Expired or Invalid JWT Token")
    public void verifyCertificate_whenInvalidToken_thenReturnsUnauthorized() throws Exception {
        String certificateId = "2e838fd1-c954-4cb2-bad9-beeeb99a55cd";
        String invalidJwtToken = "invalid-jwt-token"; // Invalid JWT token
        String url = "/api/v1/certificate/verify/" + certificateId;
        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidJwtToken) // Set the Authorization header
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Verify Certificate Successfully")
    public void verifyCertificateForResponse_whenValidCertificateId_thenReturnsSuccessMessage() throws Exception {
        String certificateId = "2e838fd1-c954-4cb2-bad9-beeeb99a55cd";
        String url = "/api/v1/certificate/verify/" + certificateId;
        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE))
                .andExpect(result -> {
                    byte[] responseBody = result.getResponse().getContentAsByteArray();
                    assertThat(responseBody).isNotEmpty();
                });
    }

    @Test
    @DisplayName("Verify Certificate with Invalid ID")
    public void verifyCertificateForResponse_whenInvalidCertificateId_thenReturnsBadRequest() throws Exception {
        String invalidCertificateId = "2e838213fd1-c954-4cb2-bad9-beeeb99a55cd";
        String url = "/api/v1/certificate/verify/" + invalidCertificateId;
        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Certificate not verified with provided uuid."));
    }
    @Test
    @DisplayName("Should successfully verify the certificate")
    public void testVerifyCertificate_Success() throws Exception {
        String url = "/api/v1/certificate/verify/to/" + validCertificateId;

        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Certificate is verified successfully."))
                .andExpect(jsonPath("$.data").value("Certificate is verified successfully."));
    }

    @Test
    @DisplayName("Should successfully verify the certificate")
    public void testVerifyCertificateWithoutJwt_Success() throws Exception {
        String url = "/api/v1/certificate/verify/to/" + validCertificateId;

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Certificate is verified successfully."))
                .andExpect(jsonPath("$.data").value("Certificate is verified successfully."));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for an invalid certificate ID")
    public void testVerifyCertificate_InvalidCertificateId() throws Exception {
        String url = "/api/v1/certificate/verify/to/" + invalidCertificateId;

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Certificate is not valid."));
    }

    @Test
    @DisplayName("Should return 404 Not Found when certificate ID is missing")
    public void testVerifyCertificate_MissingCertificateId() throws Exception {
        String url = "/api/v1/certificate/verify/to/";

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}