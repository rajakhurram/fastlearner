package com.vinncorp.fast_learner.mock.subscription.subscribed_user_profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailRequest;
import com.vinncorp.fast_learner.dtos.payment.payment_profile.PaymentProfileDetailResponse;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscribedUserProfile;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.subscription.SubscribedUserProfileRepository;
import com.vinncorp.fast_learner.response.customer_profile.CustomerProfileMaskedType;
import com.vinncorp.fast_learner.response.customer_profile.GetCustomerProfileResponse;
import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import com.vinncorp.fast_learner.response.message.MessagesType;
import com.vinncorp.fast_learner.response.subscription.*;
import com.vinncorp.fast_learner.services.payment.additional_service.IPaymentAdditionalSubscriptionService;
import com.vinncorp.fast_learner.services.payment.payment_profile.IPaymentProfileService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.subscribed_user_profile.SubscribedUserProfileService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.LogMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SubscribedUserProfileServiceTest {

    @Mock
    private SubscribedUserProfileRepository repo;

    @Mock
    private ISubscribedUserService subscribedUserService;

    @Mock
    private IPaymentProfileService paymentProfileService;

    @Mock
    private IPaymentAdditionalSubscriptionService paymentAdditionalSubscriptionService;

    @Spy
    @InjectMocks
    private SubscribedUserProfileService subscribedUserProfileService;

    private SubscribedUser subscribedUser;
    private SubscribedUserProfile subscribedUserProfile;
    private GetCustomerPaymentProfileResponse customerPaymentProfileResponse;
    private GetCustomerProfileResponse customerProfileResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscribedUser = new SubscribedUser();
        subscribedUser.setId(1L);
        subscribedUser.setUser(User.builder().id(1L).email("qasim@vinncorp.com").build());
        subscribedUser.setCustomerProfileId("cust123");
        subscribedUser.setPaymentSubscriptionId("49843748593");

        subscribedUserProfile = new SubscribedUserProfile();
        subscribedUserProfile.setId(1L);
        subscribedUserProfile.setSubscribedUser(subscribedUser);
        subscribedUserProfile.setCustomerPaymentId("cust123");
        subscribedUserProfile.setCustomerPaymentProfileId("490334905");
        subscribedUserProfile.setIsDefault(true);

        customerPaymentProfileResponse = new GetCustomerPaymentProfileResponse();

        var customerPaymentProfileMaskedType = new CustomerPaymentProfileMaskedType();
        customerPaymentProfileMaskedType.setCustomerPaymentProfileId("490334905");

        var creditCardMaskedType = new CreditCardMaskedType();
        creditCardMaskedType.setCardNumber("5424000000000015");
        creditCardMaskedType.setCardType("Master");
        creditCardMaskedType.setExpirationDate("2029-10");

        var billToMaskedType = new CustomerAddressType();
        billToMaskedType.setFirstName("Qasim");
        billToMaskedType.setLastName("Ali");

        var paymentMaskedType = new PaymentMaskedType();
        paymentMaskedType.setCreditCard(creditCardMaskedType);
        customerPaymentProfileMaskedType.setBillTo(billToMaskedType);
        customerPaymentProfileMaskedType.setPayment(paymentMaskedType);
        customerPaymentProfileResponse.setPaymentProfile(customerPaymentProfileMaskedType);

        customerProfileResponse = new GetCustomerProfileResponse();

        var customerProfileMaskedType = new CustomerProfileMaskedType();
        customerProfileResponse.setProfile(customerProfileMaskedType);
    }

    @Test
    @DisplayName("Fetch customer default payment profile by email")
    void getDefaultSubscribedUserProfile_ShouldReturnProfile() throws EntityNotFoundException, InternalServerException {
        when(subscribedUserService.findByUser(anyString())).thenReturn(subscribedUser);
        when(repo.findByIsDefaultAndSubscribedUser(eq(true), eq(subscribedUser))).thenReturn(Optional.of(subscribedUserProfile));
        when(paymentProfileService.getCustomerPaymentProfile(anyString(), anyString())).thenReturn(customerPaymentProfileResponse);

        Message<PaymentProfileDetailResponse> response = subscribedUserProfileService.getDefaultSubscribedUserProfile("test@example.com");

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Successfully get the default payment profile.", response.getMessage());
    }

    @Test
    @DisplayName("Fetch customer default payment profile with non existing email")
    void getDefaultSubscribedUserProfile_ShouldThrowEntityNotFoundException() throws EntityNotFoundException {
        when(subscribedUserService.findByUser(anyString())).thenReturn(subscribedUser);
        when(repo.findByIsDefaultAndSubscribedUser(eq(true), eq(subscribedUser))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                subscribedUserProfileService.getDefaultSubscribedUserProfile("test@example.com")
        );

        assertEquals("No profile is set to default", exception.getMessage());
    }

    @Test
    @DisplayName("Update default payment profile with valid data")
    void updatePaymentProfileDefaultStatus_whenValidIdAndStatusTrue_thenUpdateProfileAsDefault() throws Exception {
        // Arrange
        when(repo.findById(1L)).thenReturn(Optional.of(subscribedUserProfile));

        // Act
        Message<String> response = subscribedUserProfileService.updatePaymentProfileDefaultStatus(1L, true);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Successfully update the payment profile status.", response.getMessage());
        assertEquals("Payment Profile default status updated successfully!", response.getData());

        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(subscribedUserProfile);
        assertTrue(subscribedUserProfile.getIsDefault());
    }

    @DisplayName("Update payment profile when id and status is false")
    @Test
    void updatePaymentProfileDefaultStatus_whenValidIdAndStatusFalse_thenUpdateProfileAsNotDefault() throws Exception {
        // Arrange
        subscribedUserProfile.setIsDefault(false);
        when(repo.findById(1L)).thenReturn(Optional.of(subscribedUserProfile));

        // Act
        Message<String> response = subscribedUserProfileService.updatePaymentProfileDefaultStatus(1L, false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Successfully update the payment profile status.", response.getMessage());
        assertEquals("Payment Profile default status updated successfully!", response.getData());

        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(subscribedUserProfile);
        assertFalse(subscribedUserProfile.getIsDefault());
    }

    @DisplayName("Update payment profile when subscribed user profile not found")
    @Test
    void updatePaymentProfileDefaultStatus_whenProfileNotFound_thenThrowsEntityNotFoundException() {
        // Arrange
        when(repo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            subscribedUserProfileService.updatePaymentProfileDefaultStatus(1L, true);
        });

        assertEquals("Subscribed user with id " + 1L + " not exist", exception.getMessage());

        verify(repo, times(1)).findById(1L);
        verify(repo, never()).save(any(SubscribedUserProfile.class));
    }

    @DisplayName("Update payment profile when database issue occurred")
    @Test
    void updatePaymentProfileDefaultStatus_whenSavingFails_thenThrowsInternalServerException() {
        // Arrange
        when(repo.findById(1L)).thenReturn(Optional.of(subscribedUserProfile));
        doThrow(new RuntimeException("DB Error")).when(repo).save(any(SubscribedUserProfile.class));

        // Act & Assert
        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            subscribedUserProfileService.updatePaymentProfileDefaultStatus(1L, true);
        });

        assertEquals("Subscribed user profilecannot be saved due to database error.", exception.getMessage());

        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(subscribedUserProfile);
    }

    @DisplayName("Update payment profile when status is true but the default is not marked")
    @Test
    void updatePaymentProfileDefaultStatus_whenStatusTrueAndProfileNotDefault_thenMarkAllAsNotDefault() throws Exception {
        // Arrange
        when(repo.findById(1L)).thenReturn(Optional.of(subscribedUserProfile));
        subscribedUserProfile.setIsDefault(false);

        // Act
        Message<String> response = subscribedUserProfileService.updatePaymentProfileDefaultStatus(1L, true);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.toString(), response.getCode());
        assertEquals("Successfully update the payment profile status.", response.getMessage());
        assertEquals("Payment Profile default status updated successfully!", response.getData());

        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(subscribedUserProfile);
        assertTrue(subscribedUserProfile.getIsDefault());
        // Assume markAllProfileSetAsNotDefaultById is a method in YourServiceClass
        verify(subscribedUserProfileService, times(1)).markAllProfileSetAsNotDefaultById(subscribedUserProfile.getSubscribedUser().getId());
    }

    @DisplayName("Save subscribed user profile when subscribed user does not have a customer profile ID")
    @Test
    void saveSubscribedUserProfile_whenSubscribedUserDoesNotHaveCustomerProfileId_thenSetCustomerProfileId() throws Exception {
        String customerProfileId = "profile123";
        String customerPaymentProfileId = "payment123";
        String email = "test@example.com";
        Boolean isDefault = true;

        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setCustomerProfileId(null);

        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findByCustomerPaymentProfileId(customerPaymentProfileId)).thenReturn(Optional.empty());

        subscribedUserProfileService.saveSubscribedUserProfile(customerProfileId, customerPaymentProfileId, email, isDefault);

        verify(subscribedUserService, times(1)).save(subscribedUser);
        assertEquals(customerProfileId, subscribedUser.getCustomerProfileId());
        verify(repo, times(1)).findByCustomerPaymentProfileId(customerPaymentProfileId);
    }

    @DisplayName("Save subscribed user profile when subscribed user profile exist and is default")
    @Test
    void saveSubscribedUserProfile_whenSubscribedUserProfileExistsAndIsDefault_thenMarkAllAsNotDefault() throws Exception {
        String customerProfileId = "profile123";
        String customerPaymentProfileId = "payment123";
        String email = "test@example.com";
        Boolean isDefault = true;

        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setId(1L);
        subscribedUser.setCustomerProfileId(customerProfileId);

        SubscribedUserProfile subscribedUserProfile = new SubscribedUserProfile();
        subscribedUserProfile.setId(1L);
        subscribedUserProfile.setCustomerPaymentProfileId(customerPaymentProfileId);
        subscribedUserProfile.setCustomerPaymentId(customerProfileId);
        subscribedUserProfile.setIsDefault(false);

        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findByCustomerPaymentProfileId(customerPaymentProfileId)).thenReturn(Optional.of(subscribedUserProfile));

        subscribedUserProfileService.saveSubscribedUserProfile(customerProfileId, customerPaymentProfileId, email, isDefault);

        verify(subscribedUserService, never()).save(subscribedUser);
        verify(repo, times(1)).findByCustomerPaymentProfileId(customerPaymentProfileId);
        verify(subscribedUserProfileService, times(1)).markAllProfileSetAsNotDefaultById(subscribedUser.getId());
    }

    @DisplayName("Save new subscribed user profile when profile doesn't exists and is default")
    @Test
    void saveSubscribedUserProfile_whenSubscribedUserProfileDoesNotExistAndIsDefault_thenSaveNewProfileAsDefault() throws Exception {
        String customerProfileId = "profile123";
        String customerPaymentProfileId = "payment123";
        String email = "test@example.com";
        Boolean isDefault = true;

        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setId(1L);
        subscribedUser.setCustomerProfileId(customerProfileId);

        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findByCustomerPaymentProfileId(customerPaymentProfileId)).thenReturn(Optional.empty());

        subscribedUserProfileService.saveSubscribedUserProfile(customerProfileId, customerPaymentProfileId, email, isDefault);

        verify(subscribedUserService, never()).save(subscribedUser);
        verify(repo, times(1)).findByCustomerPaymentProfileId(customerPaymentProfileId);
        verify(subscribedUserProfileService, times(1)).markAllProfileSetAsNotDefaultById(subscribedUser.getId());
    }

    @DisplayName("Save subscribed user profile when subscribed user have customer profile id")
    @Test
    void saveSubscribedUserProfile_whenSubscribedUserHasCustomerProfileId_thenDoNotSetCustomerProfileId() throws Exception {
        String customerProfileId = "profile123";
        String customerPaymentProfileId = "payment123";
        String email = "test@example.com";
        Boolean isDefault = false;

        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setId(1L);
        subscribedUser.setCustomerProfileId(customerProfileId);

        SubscribedUserProfile subscribedUserProfile = new SubscribedUserProfile();
        subscribedUserProfile.setId(1L);
        subscribedUserProfile.setCustomerPaymentId(customerProfileId);
        subscribedUserProfile.setCustomerPaymentProfileId(customerPaymentProfileId);
        subscribedUserProfile.setIsDefault(false);

        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findByCustomerPaymentProfileId(customerPaymentProfileId)).thenReturn(Optional.of(subscribedUserProfile));

        subscribedUserProfileService.saveSubscribedUserProfile(customerProfileId, customerPaymentProfileId, email, isDefault);

        verify(subscribedUserService, never()).save(subscribedUser);
        verify(repo, times(1)).findByCustomerPaymentProfileId(customerPaymentProfileId);
        verify(subscribedUserProfileService, never()).markAllProfileSetAsNotDefaultById(subscribedUser.getId());
    }

    @DisplayName("Get subscribed user profile with valid ID")
    @Test
    void getSubscribedUserProfileById_whenSubscribedUserProfileExists_thenReturnSubscribedUserProfile() throws Exception {
        var subscribedUserProfile = SubscribedUserProfileTestData.subscribedUserProfile();
        subscribedUserProfile.setCustomerPaymentProfileId("490334905");
        when(repo.findById(1L)).thenReturn(Optional.of(subscribedUserProfile));

        SubscribedUserProfile result = subscribedUserProfileService.getSubscribedUserProfileById(1L);

        assertEquals(subscribedUserProfile, result);
        verify(repo, times(1)).findById(1L);
    }

    @DisplayName("Get subscribed user profile with invalid ID")
    @Test
    void getSubscribedUserProfileById_whenSubscribedUserProfileDoesNotExist_thenThrowsEntityNotFoundException() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            subscribedUserProfileService.getSubscribedUserProfileById(1L);
        });

        assertEquals("Subscribed user profile with id 1" + LogMessage.NOT_EXIST, exception.getMessage());
        verify(repo, times(1)).findById(1L);
    }

    @DisplayName("Get payment profile when payment profile exists")
    @Test
    void getPaymentProfileById_whenValidId_thenReturnsPaymentProfileDetail() throws Exception {
        // Arrange
        Long id = 1L;
        SubscribedUserProfile subscribedUserProfile = new SubscribedUserProfile();
        subscribedUserProfile.setSubscribedUser(new SubscribedUser());
        subscribedUserProfile.setCustomerPaymentId("cust123");
        subscribedUserProfile.getSubscribedUser().setCustomerProfileId("cust_profile_id");
        subscribedUserProfile.setCustomerPaymentProfileId("cust_payment_profile_id");

        GetCustomerPaymentProfileResponse profileResponse = mock(GetCustomerPaymentProfileResponse.class);
        PaymentProfileDetailResponse paymentProfileDetailResponse = mock(PaymentProfileDetailResponse.class);

        when(repo.findById(id)).thenReturn(Optional.of(subscribedUserProfile));
        when(subscribedUserProfileService.getSubscribedUserProfileById(id)).thenReturn(subscribedUserProfile);
        when(paymentProfileService.getCustomerPaymentProfile(subscribedUserProfile.getSubscribedUser().getCustomerProfileId(), subscribedUserProfile.getCustomerPaymentProfileId())).thenReturn(profileResponse);

        // Act & Assert (mocking static method)
        try (MockedStatic<PaymentProfileDetailResponse> mockedStatic = Mockito.mockStatic(PaymentProfileDetailResponse.class)) {
            mockedStatic.when(() -> PaymentProfileDetailResponse.mapToPaymentProfileResponse(subscribedUserProfile, profileResponse))
                    .thenReturn(paymentProfileDetailResponse);

            // Act
            Message<PaymentProfileDetailResponse> result = subscribedUserProfileService.getPaymentProfileById(id);

            // Assert
            assertEquals(HttpStatus.OK.value(), result.getStatus());
            assertEquals(HttpStatus.OK.toString(), result.getCode());
            assertEquals("Successfully get the payment profile.", result.getMessage());
            assertEquals(paymentProfileDetailResponse, result.getData());

            // Verify static method invocation
            mockedStatic.verify(() -> PaymentProfileDetailResponse.mapToPaymentProfileResponse(subscribedUserProfile, profileResponse), times(1));
        }

        verify(subscribedUserProfileService, times(1)).getSubscribedUserProfileById(id);
        verify(paymentProfileService, times(1)).getCustomerPaymentProfile(subscribedUserProfile.getSubscribedUser().getCustomerProfileId(), subscribedUserProfile.getCustomerPaymentProfileId());
    }

    @DisplayName("Get payment profile when profile not found")
    @Test
    void getPaymentProfileById_whenSubscribedUserProfileDoesNotExist_thenThrowsEntityNotFoundException() throws InternalServerException, EntityNotFoundException {
        // Arrange
        Long id = 1L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> subscribedUserProfileService.getPaymentProfileById(id));

        // Verify that the method to fetch the subscribed user profile was called once
        verify(subscribedUserProfileService, times(1)).getSubscribedUserProfileById(id);

        // Verify that the payment profile service was never called
        verify(paymentProfileService, never()).getCustomerPaymentProfile(anyString(), anyString());
    }

    @DisplayName("Get payment profile when payment gateway failed")
    @Test
    void getPaymentProfileById_whenPaymentProfileServiceFails_thenThrowsInternalServerException() throws Exception {
        Long id = 1L;

        SubscribedUserProfile subscribedUserProfile = new SubscribedUserProfile();
        subscribedUserProfile.setId(id);
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setCustomerProfileId("cust_profile_id");
        subscribedUserProfile.setSubscribedUser(subscribedUser);
        subscribedUserProfile.setCustomerPaymentId("cust123");
        subscribedUserProfile.setCustomerPaymentProfileId("payment_profile_id");

        when(repo.findById(id)).thenReturn(Optional.of(subscribedUserProfile));
        when(subscribedUserProfileService.getSubscribedUserProfileById(id)).thenReturn(subscribedUserProfile);
        when(paymentProfileService.getCustomerPaymentProfile("cust_profile_id", "payment_profile_id"))
                .thenThrow(new InternalServerException("Payment profile service failed."));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            subscribedUserProfileService.getPaymentProfileById(id);
        });

        assertEquals("Payment profile service failed.", exception.getMessage());
        verify(subscribedUserProfileService, times(1)).getSubscribedUserProfileById(id);
        verify(paymentProfileService, times(1))
                .getCustomerPaymentProfile("cust_profile_id", "payment_profile_id");
    }

    @DisplayName("Fetch all payment profile when provided valid data")
    @Test
    void getAllPaymentProfiles_whenProfilesFound_thenReturnsPaymentProfiles() throws Exception {
        // Arrange
        String email = "test@example.com";
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setCustomerProfileId("cust_profile_id");
        List<SubscribedUserProfile> subscribedUserProfiles = Arrays.asList(new SubscribedUserProfile(), new SubscribedUserProfile());
        GetCustomerProfileResponse profileResponseList = mock(GetCustomerProfileResponse.class);
        PaymentProfileDetailResponse paymentProfileDetailResponse = mock(PaymentProfileDetailResponse.class);

        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findAllBySubscribedUserOrderByIdDesc(subscribedUser)).thenReturn(subscribedUserProfiles);
        when(paymentProfileService.getCustomerPaymentProfileList(subscribedUser.getCustomerProfileId())).thenReturn(profileResponseList);
        when(profileResponseList.getProfile()).thenReturn(mock(CustomerProfileMaskedType.class));
        when(profileResponseList.getProfile().getPaymentProfiles()).thenReturn(Arrays.asList(mock(CustomerPaymentProfileMaskedType.class)));

        // Act & Assert (mocking static method)
        try (MockedStatic<PaymentProfileDetailResponse> mockedStatic = Mockito.mockStatic(PaymentProfileDetailResponse.class)) {
            mockedStatic.when(() -> PaymentProfileDetailResponse.mapToPaymentProfileResponse(anyList(), anyList()))
                    .thenReturn(Collections.singletonList(paymentProfileDetailResponse));

            // Act
            Message<List<PaymentProfileDetailResponse>> result = subscribedUserProfileService.getAllPaymentProfiles(email);

            // Assert
            assertEquals(HttpStatus.OK.value(), result.getStatus());
            assertEquals(HttpStatus.OK.toString(), result.getCode());
            assertEquals("Successfully get all the profiles.", result.getMessage());
            assertEquals(1, result.getData().size());

            // Verify static method invocation
            mockedStatic.verify(() -> PaymentProfileDetailResponse.mapToPaymentProfileResponse(anyList(), anyList()), times(1));
        }

        verify(subscribedUserService, times(1)).findByUser(email);
        verify(repo, times(1)).findAllBySubscribedUserOrderByIdDesc(subscribedUser);
        verify(paymentProfileService, times(1)).getCustomerPaymentProfileList(subscribedUser.getCustomerProfileId());
    }

    @DisplayName("Fetch all payment profile with invalid data")
    @Test
    void getAllPaymentProfiles_whenUserHasNoCustomerProfileId_thenThrowsEntityNotFoundException() throws Exception {
        // Arrange
        String email = "test@example.com";
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setCustomerProfileId(null);
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            subscribedUserProfileService.getAllPaymentProfiles(email);
        });

        assertEquals("No customer profile id found for the user.", exception.getMessage());
        verify(subscribedUserService, times(1)).findByUser(email);
        verify(repo, never()).findAllBySubscribedUserOrderByIdDesc(any());
        verify(paymentProfileService, never()).getCustomerPaymentProfileList(anyString());
    }

    @DisplayName("Fetch all payment profile when no profile found")
    @Test
    void getAllPaymentProfiles_whenNoProfilesFound_thenThrowsEntityNotFoundException() throws Exception {
        // Arrange
        String email = "test@example.com";
        SubscribedUser subscribedUser = new SubscribedUser();
        subscribedUser.setCustomerProfileId("cust_profile_id");
        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);
        when(repo.findAllBySubscribedUserOrderByIdDesc(subscribedUser)).thenReturn(Collections.emptyList());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            subscribedUserProfileService.getAllPaymentProfiles(email);
        });

        assertEquals("No profile found for user " + email, exception.getMessage());
        verify(subscribedUserService, times(1)).findByUser(email);
        verify(repo, times(1)).findAllBySubscribedUserOrderByIdDesc(subscribedUser);
        verify(paymentProfileService, never()).getCustomerPaymentProfileList(anyString());
    }

    @Test
    @DisplayName("Delete subscribed user profile with valid subscribed user profile id")
    void deleteSubscribedUserProfile_ShouldDeleteProfile() throws EntityNotFoundException, InternalServerException, BadRequestException {
        subscribedUser.setPaymentSubscriptionId(null);
        subscribedUserProfile.setSubscribedUser(subscribedUser);
        when(repo.findById(anyLong())).thenReturn(Optional.of(subscribedUserProfile));
        doNothing().when(paymentProfileService).deleteCustomerPaymentProfile(anyString(), anyString(), any(SubscribedUser.class));
        doNothing().when(repo).deleteById(anyLong());

        Message<String> response = subscribedUserProfileService.deleteSubscribedUserProfile(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Payment profile delete successfully.", response.getMessage());
    }

    @DisplayName("Delete subscribed user profile with invalid subscribed user profile id")
    @Test
    void deleteSubscribedUserProfile_whenProfileDoesNotExist_thenThrowsEntityNotFoundException() {
        Long id = 1L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> subscribedUserProfileService.deleteSubscribedUserProfile(id));
        verify(repo, times(1)).findById(id);
        verify(repo, never()).delete(any(SubscribedUserProfile.class));
    }

    @DisplayName("Delete subscribed user profile with invalid subscribed user profile id")
    @Test
    void deleteSubscribedUserProfile_whenDeletingPaymentProfileFails_thenThrowsInternalServerException() throws EntityNotFoundException, InternalServerException, BadRequestException {
        Long id = 1L;
        SubscribedUserProfile subscribedUserProfile = mock(SubscribedUserProfile.class);
        SubscribedUser subscribedUser = mock(SubscribedUser.class);
        User user = mock(User.class);

        when(repo.findById(id)).thenReturn(Optional.of(subscribedUserProfile));
        when(subscribedUserProfile.getSubscribedUser()).thenReturn(subscribedUser);
        when(subscribedUser.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("test@example.com");
        doThrow(new InternalServerException("Failed to delete payment profile")).when(subscribedUserProfileService).deletingPaymentProfileFromPaymentServer(subscribedUserProfile);

        assertThrows(InternalServerException.class, () -> subscribedUserProfileService.deleteSubscribedUserProfile(id));
        verify(repo, times(1)).findById(id);
        verify(repo, never()).delete(any(SubscribedUserProfile.class));
    }

    @DisplayName("Add/Update customer payment profile when provided duplicate profile")
    @Test
    void addUpdateCustomerProfile_whenAddingDuplicateProfile_thenThrowsEntityAlreadyExistException() throws Exception {
        // Arrange
        String email = "test@example.com";
        PaymentProfileDetailRequest profileDetailRequest = new PaymentProfileDetailRequest();
        SubscribedUser user = mock(SubscribedUser.class);

        when(subscribedUserService.findByUser(email)).thenReturn(user);
        when(user.getCustomerProfileId()).thenReturn("custProfileId");

        CreateCustomerPaymentProfileResponse response = new CreateCustomerPaymentProfileResponse();

        com.vinncorp.fast_learner.response.message.Message message = new com.vinncorp.fast_learner.response.message.Message();
        message.setCode(LogMessage.DUPLICATE_PROFILE_CODE);

        MessagesType messagesType = new MessagesType();
        messagesType.setMessage(List.of(message));
        messagesType.setResultCode(MessageTypeEnum.ERROR);

        response.setMessages(messagesType);

        when(paymentProfileService.createCustomerPaymentProfile(anyString(), any(), anyString())).thenReturn(response);

        // Act & Assert
        assertThrows(EntityAlreadyExistException.class, () -> subscribedUserProfileService.addUpdateCustomerProfile(email, profileDetailRequest));

        verify(subscribedUserService, times(1)).findByUser(email);
        verify(paymentProfileService, times(1)).createCustomerPaymentProfile(anyString(), any(), anyString());
    }

    @DisplayName("Add/Update customer payment profile when provided new user profile")
    @Test
    void addUpdateCustomerProfile_whenAddingNewProfile_thenSavesSuccessfully() throws Exception {
        // Arrange
        String email = "test@example.com";
        PaymentProfileDetailRequest profileDetailRequest = new PaymentProfileDetailRequest();
        SubscribedUser user = mock(SubscribedUser.class);

        when(subscribedUserService.findByUser(email)).thenReturn(user);
        when(user.getCustomerProfileId()).thenReturn("custProfileId");

        CreateCustomerPaymentProfileResponse response = new CreateCustomerPaymentProfileResponse();

        com.vinncorp.fast_learner.response.message.Message message = new com.vinncorp.fast_learner.response.message.Message();
        message.setCode(LogMessage.NOT_EXIST);

        MessagesType messagesType = new MessagesType();
        messagesType.setMessage(List.of(message));
        messagesType.setResultCode(MessageTypeEnum.OK);

        response.setMessages(messagesType);
        response.setCustomerProfileId("cust-111");
        response.setCustomerPaymentProfileId("newProfileId");

        when(paymentProfileService.createCustomerPaymentProfile(anyString(), any(), anyString())).thenReturn(response);
        when(paymentAdditionalSubscriptionService.validateCardVerification(any(), any())).thenReturn(true);
        // Act
        Message<String> result = subscribedUserProfileService.addUpdateCustomerProfile(email, profileDetailRequest);

        // Assert
        assertNotEquals(result, null);
        assertEquals(result.getStatus(), HttpStatus.OK.value());
        assertEquals(result.getData(), "Successfully added customer payment profile with id newProfileId");
        verify(subscribedUserService, times(1)).findByUser(email);
        verify(paymentProfileService, times(1)).createCustomerPaymentProfile(anyString(), any(), anyString());
        verify(repo, times(1)).save(any(SubscribedUserProfile.class));
    }

    // TODO ERROR: Resolve below test method error
    @DisplayName("Add/Update customer payment profile when provided existing profile")
    @Test
    void addUpdateCustomerProfile_whenUpdatingExistingProfile_thenUpdatesSuccessfully() throws Exception {
        // Arrange
        String email = "test@example.com";
        PaymentProfileDetailRequest profileDetailRequest = new PaymentProfileDetailRequest();
        profileDetailRequest.setId(1L);
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        SubscribedUserProfile subscribedUserProfile = SubscribedUserProfileTestData.subscribedUserProfile();
        subscribedUserProfile.setCustomerPaymentProfileId("customerPaymentProfileId");
        subscribedUserProfile.setSubscribedUser(subscribedUser);


        when(repo.findById(1L)).thenReturn(Optional.of(subscribedUserProfile));

        // Act
        Message<String> result = subscribedUserProfileService.addUpdateCustomerProfile(email, profileDetailRequest);

        // Assert
        assertNotEquals(result, null);
        assertEquals(result.getStatus(), HttpStatus.OK.value());
        assertEquals(result.getData(), "Successfully update customer payment profile with id " + subscribedUserProfile.getCustomerPaymentProfileId());
        verify(repo, times(1)).findById(1L);
        verify(paymentProfileService, times(1)).updateCustomerPaymentProfile(anyString(), anyString(), any(), anyString());
    }

    @DisplayName("Add/Update customer payment profile when provided user having no customer profile")
    @Test
    void addUpdateCustomerProfile_whenUserHasNoCustomerProfileId_thenThrowsBadRequestException() throws Exception{
        // Arrange
        String email = "test@example.com";
        PaymentProfileDetailRequest profileDetailRequest = new PaymentProfileDetailRequest();
        User user = UserTestData.userData();
        SubscribedUser subscribedUser = SubscribedUserTestData.subscribedUser();
        subscribedUser.setCustomerProfileId(null);
        subscribedUser.setUser(user);

        when(subscribedUserService.findByUser(email)).thenReturn(subscribedUser);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> subscribedUserProfileService.addUpdateCustomerProfile(email, profileDetailRequest));

        verify(subscribedUserService, times(1)).findByUser(email);
        verify(paymentProfileService, never()).createCustomerPaymentProfile(anyString(), any(), anyString());
    }
}