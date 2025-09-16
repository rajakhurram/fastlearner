package com.vinncorp.fast_learner.mock.affiliate;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.vinncorp.fast_learner.models.affiliate.Affiliate;
import com.vinncorp.fast_learner.models.affiliate.InstructorAffiliate;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.affiliate.AffiliateRepository;
import com.vinncorp.fast_learner.repositories.affiliate.InstructorAffiliateRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.affiliate.CreateAffiliateReq;
import com.vinncorp.fast_learner.response.affiliate.AffiliateDetailResponse;
import com.vinncorp.fast_learner.response.affiliate.AffiliationResponse;
import com.vinncorp.fast_learner.response.affiliate.AffiliationResponseByPaginated;
import com.vinncorp.fast_learner.services.affiliate.affiliate_service.AffiliateService;
import com.vinncorp.fast_learner.services.email_template.EmailService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.TimeUtil;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.util.*;

public class AffiliateServiceTest {
    @Mock
    private InstructorAffiliateRepository instructorAffiliateRepository; // Replace with your actual repository

    @Mock
    private EmailService emailService;
    @Mock
    private AffiliateRepository affiliateRepository;
    @InjectMocks
    private AffiliateService affiliateService; // Replace with your actual service class

    private Long instructorId;
    private Pageable pageable;
    private CreateAffiliateReq request;

    @Mock
    private UserRepository userRepository;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        instructorId = 1L; // Example instructor ID
        pageable = mock(Pageable.class); // Mock the Pageable for tests


    }


    @Test
    @DisplayName("Should return NOT_FOUND when instructor is not found")
    void testGetAffiliateByInstructor_NoInstructor() {
        // Arrange
        String email = "nonexistent@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Message<AffiliationResponseByPaginated> response = affiliateService.getAffiliateByInstructor("search", principal, pageable);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Instructor not found with email: nonexistent@example.com", response.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoInteractions(instructorAffiliateRepository);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when no affiliates are found")
    void testGetAffiliateByInstructor_NoAffiliates() {
        // Arrange
        String email = "instructor@example.com";
        User instructor = new User();
        instructor.setId(1L);
        instructor.setFullName("John Doe");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(instructor));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Tuple> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(instructorAffiliateRepository.findByInstructorAndIsActive("search", instructor.getId(), email, pageable))
                .thenReturn(emptyPage);

        // Act
        Message<AffiliationResponseByPaginated> response = affiliateService.getAffiliateByInstructor("search", principal, pageable);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("No affiliates found for instructor ID: John Doe", response.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verify(instructorAffiliateRepository, times(1))
                .findByInstructorAndIsActive("search", instructor.getId(), null, pageable);
    }

    @Test
    @DisplayName("Should handle exceptions gracefully")
    void testGetAffiliateByInstructor_ExceptionHandling() {
        // Arrange
        String email = "instructor@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Database error"));

        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        try {
            affiliateService.getAffiliateByInstructor("search", principal, pageable);
        } catch (Exception ex) {
            assertEquals("Database error", ex.getMessage());
        }
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoInteractions(instructorAffiliateRepository);
    }

//delete affiliate
    @Test
    @DisplayName("Should mark affiliate as inactive when ID is found")
    void testDeleteAffiliateByInstructor_Success() {
        // Arrange
        Long affiliateId = 1L;

        InstructorAffiliate instructorAffiliate = new InstructorAffiliate();
        instructorAffiliate.setId(affiliateId);
        instructorAffiliate.setStatus(GenericStatus.ACTIVE);

        when(instructorAffiliateRepository.findById(affiliateId))
                .thenReturn(Optional.of(instructorAffiliate));

        // Act
        Message response = affiliateService.deleteAffiliateByInstructor(affiliateId);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Affiliate deleted successfully", response.getMessage());
        verify(instructorAffiliateRepository, times(1)).findById(affiliateId);
        verify(instructorAffiliateRepository, times(1)).save(instructorAffiliate);
        assertEquals(GenericStatus.INACTIVE, instructorAffiliate.getStatus());
    }
    @Test
    @DisplayName("Should return NOT_FOUND when affiliate ID is not found")
    void testDeleteAffiliateByInstructor_NotFound() {
        // Arrange
        Long affiliateId = 1L;

        when(instructorAffiliateRepository.findById(affiliateId))
                .thenReturn(Optional.empty());

        // Act
        Message response = affiliateService.deleteAffiliateByInstructor(affiliateId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Affiliate not found with ID: " + affiliateId, response.getMessage());
        verify(instructorAffiliateRepository, times(1)).findById(affiliateId);
        verify(instructorAffiliateRepository, never()).save(any(InstructorAffiliate.class));
    }

    @Test
    @DisplayName("Should handle repository exceptions gracefully")
    void testDeleteAffiliateByInstructor_RepositoryException() {
        // Arrange
        Long affiliateId = 1L;

        when(instructorAffiliateRepository.findById(affiliateId))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            affiliateService.deleteAffiliateByInstructor(affiliateId);
        });
        assertEquals("Database error", exception.getMessage());
        verify(instructorAffiliateRepository, times(1)).findById(affiliateId);
        verify(instructorAffiliateRepository, never()).save(any(InstructorAffiliate.class));
    }
    @Test
    @DisplayName("Should return success even if affiliate is already inactive")
    void testDeleteAffiliateByInstructor_AlreadyInactive() {
        // Arrange
        Long affiliateId = 1L;

        InstructorAffiliate instructorAffiliate = new InstructorAffiliate();
        instructorAffiliate.setId(affiliateId);
        instructorAffiliate.setStatus(GenericStatus.INACTIVE);

        when(instructorAffiliateRepository.findById(affiliateId))
                .thenReturn(Optional.of(instructorAffiliate));

        // Act
        Message response = affiliateService.deleteAffiliateByInstructor(affiliateId);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Affiliate deleted successfully", response.getMessage());
        verify(instructorAffiliateRepository, times(1)).findById(affiliateId);
    }

    @Test
    @DisplayName("Should return NOT_FOUND for invalid or null affiliate ID")
    void testDeleteAffiliateByInstructor_InvalidId() {
        // Arrange
        Long affiliateId = null;

        // Act
        Message response = affiliateService.deleteAffiliateByInstructor(affiliateId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Affiliate not found with ID: null", response.getMessage());
        verify(instructorAffiliateRepository, never()).findById(any());
        verify(instructorAffiliateRepository, never()).save(any());
    }



    //get affiliate by instructor

    @Test
    @DisplayName("Should return NOT_FOUND when instructor is not found")
    void testGetAffiliateByInstructor_NoInstructorFound() {
        // Arrange
        String email = "nonexistent@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Message<AffiliationResponseByPaginated> response = affiliateService.getAffiliateByInstructor("search", principal, pageable);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Instructor not found with email: nonexistent@example.com", response.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoInteractions(instructorAffiliateRepository);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when no affiliates are found")
    void testGetAffiliateByInstructor_NoAffiliatesFound() {
        // Arrange
        String email = "instructor@example.com";
        Principal principal = mock(Principal.class);
        User instructor = new User();
        instructor.setId(1L);
        instructor.setEmail(email);
        instructor.setFullName("John Doe");

        when(principal.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(instructor));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Tuple> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(instructorAffiliateRepository.findByInstructorAndIsActive("search", instructor.getId(), email, pageable))
                .thenReturn(emptyPage);

        // Act
        Message<AffiliationResponseByPaginated> response = affiliateService.getAffiliateByInstructor("search", principal, pageable);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("No affiliates found for instructor ID: John Doe", response.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verify(instructorAffiliateRepository, times(1))
                .findByInstructorAndIsActive("search", instructor.getId(), email, pageable);
    }

    @Test
    @DisplayName("Should return OK with affiliation data when affiliates are found")
    void testGetAffiliateByInstructor_AffiliatesFound() {
        // Arrange
        String email = "instructor@example.com";
        Principal principal = mock(Principal.class);
        User instructor = new User();
        instructor.setId(1L);
        instructor.setEmail(email);
        instructor.setFullName("John Doe");

        when(principal.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(instructor));

        Pageable pageable = PageRequest.of(0, 10);
        List<Tuple> tupleList = createMockTuples();
        Page<Tuple> tuplePage = new PageImpl<>(tupleList, pageable, tupleList.size());
        when(instructorAffiliateRepository.findByInstructorAndIsActive("search", instructor.getId(), email, pageable))
                .thenReturn(tuplePage);

        // Act
        Message<AffiliationResponseByPaginated> response = affiliateService.getAffiliateByInstructor("search", principal, pageable);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Affiliates retrieved successfully", response.getMessage());
        assertEquals(1, response.getData().getTotalElements());
        verify(userRepository, times(1)).findByEmail(email);
        verify(instructorAffiliateRepository, times(1))
                .findByInstructorAndIsActive("search", instructor.getId(), email, pageable);
    }

    // Helper method to create mock tuples
    private List<Tuple> createMockTuples() {
        Tuple mockTuple = mock(Tuple.class);
        when(mockTuple.get("id")).thenReturn(1L);
        when(mockTuple.get("affiliation_id")).thenReturn(10L);
        when(mockTuple.get("email")).thenReturn("affiliate@example.com");
        when(mockTuple.get("username")).thenReturn("Affiliate User");
        when(mockTuple.get("nick_name")).thenReturn("Nick");
        when(mockTuple.get("default_reward")).thenReturn(100.0);
        when(mockTuple.get("is_self")).thenReturn(true);
        when(mockTuple.get("affiliate_uuid")).thenReturn("123e4567-e89b-12d3-a456-426614174000");

        return Collections.singletonList(mockTuple);
    }

    //getAffiliateUserByAffiliateId

    @Test
    @DisplayName("Should return NOT_FOUND when instructor is not found")
    void testGetAffiliateUserByAffiliateId_InstructorNotFound() {
        // Arrange
        Long instructorAffiliateId = 1L;
        String email = "nonexistent@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Message response = affiliateService.getAffiliateUserByAffiliateId(instructorAffiliateId, principal);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Instructor not found with email: nonexistent@example.com", response.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoInteractions(instructorAffiliateRepository);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when affiliate is not found")
    void testGetAffiliateUserByAffiliateId_AffiliateNotFound() {
        // Arrange
        Long instructorAffiliateId = 1L;
        String email = "instructor@example.com";
        Principal principal = mock(Principal.class);
        User instructor = new User();
        instructor.setId(1L);
        instructor.setEmail(email);

        when(principal.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(instructor));
        when(instructorAffiliateRepository.findByInstructorAff(instructorAffiliateId,instructor.getEmail())).thenReturn(null);

        // Act
        Message response = affiliateService.getAffiliateUserByAffiliateId(instructorAffiliateId, principal);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("No affiliates found for instructor affiliate ID: 1", response.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verify(instructorAffiliateRepository, times(1)).findByInstructorAff(instructorAffiliateId,instructor.getEmail());
    }

    @Test
    @DisplayName("Should return OK with affiliate details when affiliate is found")
    void testGetAffiliateUserByAffiliateId_AffiliateFound() {
        // Arrange
        Long instructorAffiliateId = 1L;
        String email = "instructor@example.com";
        Principal principal = mock(Principal.class);
        User instructor = new User();
        instructor.setId(1L);
        instructor.setEmail(email);

        Tuple mockTuple = mock(Tuple.class);
        when(mockTuple.get("affiliate_id")).thenReturn(100L);
        when(mockTuple.get("instructor_affiliate_id")).thenReturn(instructorAffiliateId);
        when(mockTuple.get("email")).thenReturn("affiliate@example.com");
        when(mockTuple.get("username")).thenReturn("Affiliate User");
        when(mockTuple.get("nick_name")).thenReturn("Nick");
        when(mockTuple.get("default_reward")).thenReturn(500.0);
        when(mockTuple.get("total_revenue")).thenReturn(1000.0);
        when(mockTuple.get("onboard_status")).thenReturn("ACTIVE");
        when(mockTuple.get("onboarded_students")).thenReturn(10L);

        when(principal.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(instructor));
        when(instructorAffiliateRepository.findByInstructorAff(instructorAffiliateId,instructor.getEmail())).thenReturn(mockTuple);

        // Act
        Message response = affiliateService.getAffiliateUserByAffiliateId(instructorAffiliateId, principal);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Affiliate detail fetched successfully", response.getMessage());
        AffiliateDetailResponse data = (AffiliateDetailResponse) response.getData();
        assertEquals(100L, data.getAffiliateId());
        assertEquals(1L, data.getInstructorAffiliateId());
        assertEquals("affiliate@example.com", data.getEmail());
        assertEquals("Affiliate User", data.getName());
        assertEquals("Nick", data.getNickName());
        assertEquals(500.0, data.getRewards());
        assertEquals(1000.0, data.getTotalRevenue());
        assertEquals("ACTIVE", data.getOnboardStatus());
        assertEquals(10L, data.getTotalOnboardedStudent());
        verify(userRepository, times(1)).findByEmail(email);
        verify(instructorAffiliateRepository, times(1)).findByInstructorAff(instructorAffiliateId,instructor.getEmail());
    }

    //update affiliate

    @Test
    @DisplayName("Should return NOT_FOUND when instructor is not found")
    void testUpdateAffiliateUser_InstructorNotFound() {
        // Arrange
        CreateAffiliateReq request = new CreateAffiliateReq();
        request.setInstructorAffiliateId(1L);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Message response = affiliateService.updateAffiliateUser(request, principal);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Instructor not found with email: nonexistent@example.com", response.getMessage());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verifyNoInteractions(instructorAffiliateRepository);
    }

    @Test
    @DisplayName("Should return CONFLICT when nickname already exists")
    void testUpdateAffiliateUser_NicknameConflict() {
        // Arrange
        CreateAffiliateReq request = new CreateAffiliateReq();
        request.setInstructorAffiliateId(1L);
        request.setNickName("ExistingNick");
        Principal principal = mock(Principal.class);
        User instructor = new User();
        instructor.setId(1L);

        when(principal.getName()).thenReturn("instructor@example.com");
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(instructorAffiliateRepository.findByInstructorAndNickNameAndNotAffUserId(
                1L, 1L, "ExistingNick")).thenReturn(new InstructorAffiliate());

        // Act
        Message response = affiliateService.updateAffiliateUser(request, principal);

        // Assert
        assertEquals(409, response.getStatus());
        assertEquals("Affiliate user already exists with the given nickname", response.getMessage());
        verify(userRepository, times(1)).findByEmail("instructor@example.com");
        verify(instructorAffiliateRepository, times(1))
                .findByInstructorAndNickNameAndNotAffUserId(1L, 1L, "ExistingNick");
    }

    @Test
    @DisplayName("Should return NOT_FOUND when instructor affiliate is not found")
    void testUpdateAffiliateUser_AffiliateNotFound() {
        // Arrange
        CreateAffiliateReq request = new CreateAffiliateReq();
        request.setInstructorAffiliateId(1L);
        request.setNickName("NewNick");
        Principal principal = mock(Principal.class);
        User instructor = new User();
        instructor.setId(1L);

        when(principal.getName()).thenReturn("instructor@example.com");
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(instructorAffiliateRepository.findByInstructorAndNickNameAndNotAffUserId(
                1L, 1L, "NewNick")).thenReturn(null);
        when(instructorAffiliateRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Message response = affiliateService.updateAffiliateUser(request, principal);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Instructor Affiliate not found with ID: 1", response.getMessage());
        verify(userRepository, times(1)).findByEmail("instructor@example.com");
        verify(instructorAffiliateRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return OK when affiliate user is updated successfully")
    void testUpdateAffiliateUser_SuccessfulUpdate() {
        // Arrange
        CreateAffiliateReq request = new CreateAffiliateReq();
        request.setInstructorAffiliateId(1L);
        request.setNickName("UpdatedNick");
        request.setName("UpdatedName");
        request.setDefaultReward(100.0);
        Principal principal = mock(Principal.class);
        User instructor = new User();
        instructor.setId(1L);

        InstructorAffiliate instructorAffiliate = new InstructorAffiliate();
        instructorAffiliate.setId(1L);

        when(principal.getName()).thenReturn("instructor@example.com");
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));
        when(instructorAffiliateRepository.findByInstructorAndNickNameAndNotAffUserId(
                1L, 1L, "UpdatedNick")).thenReturn(null);
        when(instructorAffiliateRepository.findById(1L)).thenReturn(Optional.of(instructorAffiliate));
        when(instructorAffiliateRepository.save(any(InstructorAffiliate.class))).thenReturn(instructorAffiliate);

        // Act
        Message response = affiliateService.updateAffiliateUser(request, principal);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Affiliate user updated successfully", response.getMessage());
        verify(userRepository, times(1)).findByEmail("instructor@example.com");
        verify(instructorAffiliateRepository, times(1)).findById(1L);
        verify(instructorAffiliateRepository, times(1)).save(any(InstructorAffiliate.class));
    }

    @Test
    @DisplayName("Should return NOT_FOUND when instructor is not found")
    void testStripeResendLinkForAffiliate_InstructorNotFound() throws StripeException, JsonProcessingException {
        // Arrange
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Message response = affiliateService.stripeResendLinkForAffiliate("affiliate@example.com", principal);

        // Assert
        assertEquals(404, response.getStatus());
        assertEquals("Instructor not found with email: nonexistent@example.com", response.getMessage());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verifyNoInteractions(affiliateRepository, instructorAffiliateRepository);
    }



//stripe resend link


    @Test
    @DisplayName("Should return NOT_FOUND when instructor is not found")
    void testGetAffiliateByInstructor_InstructorNotFound() {
        // Arrange
        String email = "instructor1@mailinator.com";
        String search = "search";
        Pageable pageable = PageRequest.of(0, 10);
        Principal principal = createMockPrincipal(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Message<AffiliationResponseByPaginated> response = affiliateService.getAffiliateByInstructor(search, principal, pageable);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus(), "Response status should be 404");
        assertNull(response.getData(), "Response data should be null");

        verify(userRepository).findByEmail(email);
        verify(instructorAffiliateRepository, never()).findByInstructorAndIsActive(anyString(), anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return NOT_FOUND when no affiliates are found")
    void testGetAffiliatesByInstructor_NoAffiliatesFound() {
        // Arrange
        String email = "instructor1@mailinator.com";
        String search = "search";
        Pageable pageable = PageRequest.of(0, 10);

        User instructor = createMockInstructor(1L, "John Doe");
        Principal principal = createMockPrincipal(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(instructor));
        when(instructorAffiliateRepository.findByInstructorAndIsActive(search, instructor.getId(), email, pageable))
                .thenReturn(Page.empty());

        // Act
        Message<AffiliationResponseByPaginated> response = affiliateService.getAffiliateByInstructor(search, principal, pageable);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus(), "Response status should be 404");
        assertNull(response.getData(), "Response data should be null");

        verify(userRepository).findByEmail(email);
        verify(instructorAffiliateRepository).findByInstructorAndIsActive(search, instructor.getId(), null, pageable);
    }

    // Helper methods
    private User createMockInstructor(Long id, String fullName) {
        User instructor = new User();
        instructor.setId(id);
        instructor.setFullName(fullName);
        return instructor;
    }

    private Principal createMockPrincipal(String email) {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);
        return principal;
    }

    //create affiliate


    @Test
    @DisplayName("Should create a Stripe account link successfully")
    void testCreateAccountLink_Success() throws Exception {
        // Arrange
        String email = "affiliate@mail.com";

        Affiliate affiliate = new Affiliate();
        affiliate.setEmail(email);
        when(affiliateRepository.findByEmail(email)).thenReturn(affiliate);

        Account mockAccount = mock(Account.class);
        when(mockAccount.getId()).thenReturn("acct_12345");
        mockStatic(Account.class).when(() -> Account.create(any(AccountCreateParams.class))).thenReturn(mockAccount);

        AccountLink mockAccountLink = mock(AccountLink.class);
        when(mockAccountLink.getUrl()).thenReturn("https://stripe.com/onboarding");
        mockStatic(AccountLink.class).when(() -> AccountLink.create(any(AccountLinkCreateParams.class))).thenReturn(mockAccountLink);

        // Act
        Message<String> response = affiliateService.createAccountLink(email);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Successfully connected account redirection link.", response.getMessage());
        assertTrue(response.getData().contains("accountUrl"), "Response should contain accountUrl");
        assertTrue(response.getData().contains("stripeAccount"), "Response should contain stripeAccount");

        // Verify interactions
        verify(affiliateRepository).findByEmail(email);
        verify(affiliateRepository).save(any(Affiliate.class));
    }

    @Test
    @DisplayName("Should send email with Stripe URL")
    void testSendEmailLinkForStripeUrl_WithUrl() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setEmail("affiliate@mail.com");

        String username = "Affiliate User";
        String stripeUrl = "https://stripe.com/onboarding";

        doNothing().when(emailService).sendEmail(
                eq("affiliate@mail.com"),
                eq("Welcome to the FastLearner Affiliate Program!"),
                anyString(),
                eq(true)
        );

        // Act
        Message<String> response = affiliateService.sendEmailLinkForStripeUrl(affiliate, username, stripeUrl);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Email sent successfully.", response.getMessage());
        verify(emailService).sendEmail(
                eq("affiliate@mail.com"),
                eq("Welcome to the FastLearner Affiliate Program!"),
                anyString(),
                eq(true)
        );
    }

    @Test
    @DisplayName("Should send email without Stripe URL")
    void testSendEmailLinkForStripeUrl_WithoutUrl() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setEmail("affiliate@mail.com");

        String username = "Affiliate User";
        String stripeUrl = null;

        doNothing().when(emailService).sendEmail(
                eq("affiliate@mail.com"),
                eq("Welcome to the FastLearner Affiliate Program!"),
                anyString(),
                eq(true)
        );

        // Act
        Message<String> response = affiliateService.sendEmailLinkForStripeUrl(affiliate, username, stripeUrl);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Email sent successfully.", response.getMessage());
        verify(emailService).sendEmail(
                eq("affiliate@mail.com"),
                eq("Welcome to the FastLearner Affiliate Program!"),
                anyString(),
                eq(true)
        );
    }

    @Test
    @DisplayName("Should throw exception when affiliate is null in sendEmailLinkForStripeUrl")
    void testSendEmailLinkForStripeUrl_NullAffiliate() {
        // Arrange
        Affiliate affiliate = null; // Deliberately set to null for this test case
        String username = "Affiliate User";
        String stripeUrl = "https://stripe.com/onboarding";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> affiliateService.sendEmailLinkForStripeUrl(affiliate, username, stripeUrl));
        assertEquals("Affiliate cannot be null", exception.getMessage());
    }

    // Example of creating an Affiliate object for use in other tests
    @Test
    @DisplayName("Should send email with populated Affiliate object")
    void testSendEmailLinkForStripeUrl_WithPopulatedAffiliate() {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setId(1L);
        affiliate.setEmail("affiliate@mail.com");
        affiliate.setStripeAccountId("acct_12345");
        affiliate.setCreatedStripeDate(new Date());
        affiliate.setOnboardStatus(PayoutStatus.PENDING);

        String username = "Affiliate User";
        String stripeUrl = "https://stripe.com/onboarding";

        doNothing().when(emailService).sendEmail(
                eq("affiliate@mail.com"),
                eq("Welcome to the FastLearner Affiliate Program!"),
                anyString(),
                eq(true)
        );

        // Act
        Message<String> response = affiliateService.sendEmailLinkForStripeUrl(affiliate, username, stripeUrl);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Email sent successfully.", response.getMessage());
        verify(emailService).sendEmail(
                eq("affiliate@mail.com"),
                eq("Welcome to the FastLearner Affiliate Program!"),
                anyString(),
                eq(true)
        );
    }

    @Test
    @DisplayName("Should return conflict if affiliate with same email and nickname exists")
    void testCreateAffiliateUser_Conflict() throws StripeException {
        // Arrange
        CreateAffiliateReq request = new CreateAffiliateReq(1L,"email@example.com", "Affiliate Name", "nickname", 10.0);

        Principal principal = () -> "instructor@example.com";

        Affiliate affiliate = new Affiliate();
        affiliate.setEmail(request.getEmail());
        affiliate.setOnboardStatus(PayoutStatus.PENDING);

        User instructor = new User();
        instructor.setEmail(principal.getName());

        InstructorAffiliate existingInstructorAffiliate = new InstructorAffiliate();
        existingInstructorAffiliate.setNickname(request.getNickName());
        existingInstructorAffiliate.setStatus(GenericStatus.ACTIVE);

        // Mocking repository calls
        when(affiliateRepository.findByEmail(request.getEmail())).thenReturn(affiliate);
        when(userRepository.findByEmail(principal.getName())).thenReturn(Optional.of(instructor));
        when(instructorAffiliateRepository.findByInstructorAndNickName(instructor.getId(), request.getNickName()))
                .thenReturn(existingInstructorAffiliate);

        // Act
        Message<InstructorAffiliate> response = affiliateService.createAffiliateUser(request, principal);

        // Assert
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertEquals("Affiliate user already exists with the given nickname", response.getMessage());
    }

    @Test
    @DisplayName("Should return error if instructor not found")
    void testCreateAffiliateUser_InstructorNotFound() throws StripeException {
        // Arrange
        CreateAffiliateReq request = new CreateAffiliateReq(1L,"email@example.com", "Affiliate Name", "nickname", 10.0);

        Principal principal = () -> "instructor@example.com";

        // Mocking repository calls
        when(userRepository.findByEmail(principal.getName())).thenReturn(Optional.empty()); // Instructor not found

        // Act
        Message<InstructorAffiliate> response = affiliateService.createAffiliateUser(request, principal);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Instructor not found with email: instructor@example.com", response.getMessage());
    }

    @Test
    @DisplayName("Should return validation error if defaultReward is null")
    void testCreateAffiliateUser_DefaultRewardNull() throws StripeException {
        // Arrange
        CreateAffiliateReq request = new CreateAffiliateReq(1L, "email@example.com", "Affiliate Name", "nickname", null);
        Principal principal = () -> "instructor@example.com";

        // Act
        Message<InstructorAffiliate> response = affiliateService.createAffiliateUser(request, principal);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

    }
}
