package com.vinncorp.fast_learner.mock.transaction_history;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.subscription.subscribed_user.SubscribedUserTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.tansaction_history.TransactionHistoryRepository;
import com.vinncorp.fast_learner.response.transaction_history.TransactionHistoryResponse;
import com.vinncorp.fast_learner.services.transaction_history.TransactionHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class TransactionHistoryMockService {

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @InjectMocks
    private TransactionHistoryService transactionService; // The class containing getByTransactionId
    @Mock
    private IUserService userService;


    @Test
    @DisplayName("Test getByTransactionId Success")
    void testGetByTransactionId_Success() throws EntityNotFoundException {
        // Arrange
        Long transactionId = 1L;
        TransactionHistory mockTransactionHistory = new TransactionHistory();
        mockTransactionHistory.setId(transactionId);

        Mockito.when(transactionHistoryRepository.findById(transactionId))
                .thenReturn(Optional.of(mockTransactionHistory));

        // Act
        Message<TransactionHistory> response = transactionService.getByTransactionId(transactionId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Transaction history fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(transactionId, response.getData().getId());
        verify(transactionHistoryRepository, times(1)).findById(transactionId);
    }

    @Test
    @DisplayName("Test getByTransactionId TransactionIdNull")
    void testGetByTransactionId_TransactionIdNull() {
        // Arrange
        Long transactionId = null;

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> transactionService.getByTransactionId(transactionId)
        );

        // Assert the exception message
        assertEquals("transaction id should not be null", exception.getMessage());

        // Verify that the repository method is never called
        verify(transactionHistoryRepository, never()).findById(Mockito.any());
    }

    @Test
    @DisplayName("Test getByTransactionId TransactionNotFound")
    void testGetByTransactionId_TransactionNotFound() {
        // Arrange
        Long transactionId = 2L;

        Mockito.when(transactionHistoryRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> transactionService.getByTransactionId(transactionId)
        );
        assertEquals("Transaction history not found.", exception.getMessage());
        verify(transactionHistoryRepository, times(1)).findById(transactionId);
    }

    @Test
    @DisplayName("Test getByTransactionId InvalidTransaction")
    void testGetByTransactionId_InvalidTransaction() throws EntityNotFoundException {
        // Arrange
        Long transactionId = 3L;
        TransactionHistory invalidTransactionHistory = new TransactionHistory();
        // Assume invalid data here

        Mockito.when(transactionHistoryRepository.findById(transactionId))
                .thenReturn(Optional.of(invalidTransactionHistory));

        // Act
        Message<TransactionHistory> response = transactionService.getByTransactionId(transactionId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Transaction history fetched successfully.", response.getMessage());
        assertNotNull(response.getData());
        verify(transactionHistoryRepository, times(1)).findById(transactionId);
    }

    @Test
    @DisplayName("Test fetchTransactionHistory Success")
    void testFetchTransactionHistory_Success() throws EntityNotFoundException {
        // Arrange
        String email = "user@example.com";
        User mockUser = new User();
        mockUser.setId(1L);

        TransactionHistoryResponse txn1 = new TransactionHistoryResponse();
        txn1.setId(1L);
        txn1.setSubscriptionAmount(100.0);

        TransactionHistoryResponse txn2 = new TransactionHistoryResponse();
        txn2.setId(2L);
        txn2.setSubscriptionAmount(200.0);

        List<TransactionHistoryResponse> mockTransactionHistoryList = List.of(txn1, txn2);
        Page<TransactionHistoryResponse> mockPage = new PageImpl<>(
                mockTransactionHistoryList,           // List of data
                PageRequest.of(0, mockTransactionHistoryList.size()), // Page request with page number and size
                mockTransactionHistoryList.size()     // Total elements
        );
        Mockito.when(userService.findByEmail(email)).thenReturn(mockUser);
        Mockito.when(transactionHistoryRepository.findByUserOrderByIdDesc(mockUser.getId(), PageRequest.of(0, 10))).thenReturn(mockPage);

        // Act
        Message<Page<TransactionHistoryResponse>> response = transactionService.fetchTransactionHistory(PageRequest.of(0, 10), email);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK.value(), response.getStatus(), "HTTP status should be 200");
        assertEquals("Transaction history fetched successfully.", response.getMessage(), "Message should match expected text");
        assertNotNull(response.getData(), "Response data should not be null");
        assertEquals(2, response.getData().getContent().size(), "Response data size should match");

        TransactionHistoryResponse returnedTxn1 = response.getData().toList().get(0);
        TransactionHistoryResponse returnedTxn2 = response.getData().toList().get(1);

        assertEquals(100.0, returnedTxn1.getSubscriptionAmount(), 0.01, "Amount should match for txn1");

        assertEquals(200.0, returnedTxn2.getSubscriptionAmount(), 0.01, "Amount should match for txn2");

        verify(userService, times(1)).findByEmail(email);
        verify(transactionHistoryRepository, times(1)).findByUserOrderByIdDesc(mockUser.getId(), PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Test fetchTransactionHistory UserNotFound")
    void testFetchTransactionHistory_UserNotFound() throws EntityNotFoundException {
        // Arrange
        String email = "nonexistent@example.com";

        Mockito.when(userService.findByEmail(email)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> transactionService.fetchTransactionHistory(PageRequest.of(0, 10), email)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).findByEmail(email);
        verify(transactionHistoryRepository, never()).findByUserOrderByIdDesc(Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("Test fetchTransactionHistory TransactionHistoryNotFound")
    void testFetchTransactionHistory_TransactionHistoryNotFound() throws EntityNotFoundException {
        // Arrange
        String email = "user@example.com";
        User mockUser = new User();
        mockUser.setId(1L);

        Mockito.when(userService.findByEmail(email)).thenReturn(mockUser);
        Mockito.when(transactionHistoryRepository.findByUserOrderByIdDesc(mockUser.getId(), PageRequest.of(0, 10))).thenReturn(Page.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> transactionService.fetchTransactionHistory(PageRequest.of(0, 10), email)
        );

        assertEquals("Transaction history not found", exception.getMessage());
        verify(userService, times(1)).findByEmail(email);
        verify(transactionHistoryRepository, times(1)).findByUserOrderByIdDesc(mockUser.getId(), PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Test findByAuthSubscriptionId Success")
    void testFindByAuthSubscriptionId_Success() throws EntityNotFoundException {
        // Arrange
        String authSubscriptionId = "AUTH123";
        TransactionHistory mockTransactionHistory = new TransactionHistory();
        mockTransactionHistory.setAuthSubscriptionId(authSubscriptionId);

        when(transactionHistoryRepository.findByAuthSubscriptionId(authSubscriptionId))
                .thenReturn(mockTransactionHistory);

        // Act
        TransactionHistory result = transactionService.findByAuthSubscriptionId(authSubscriptionId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(authSubscriptionId, result.getAuthSubscriptionId(), "AuthSubscriptionId should match");
        verify(transactionHistoryRepository, times(1)).findByAuthSubscriptionId(authSubscriptionId);
    }

    @Test
    @DisplayName("Test findByAuthSubscriptionId NullInput")
    void testFindByAuthSubscriptionId_NullInput() {
        // Arrange
        String authSubscriptionId = null;

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> transactionService.findByAuthSubscriptionId(authSubscriptionId),
                "Should throw EntityNotFoundException for null input");
        assertEquals("Payment subscription not found", exception.getMessage());
        verify(transactionHistoryRepository, never()).findByAuthSubscriptionId(anyString());
    }

    @Test
    @DisplayName("Test findByAuthSubscriptionId EmptyInput")
    void testFindByAuthSubscriptionId_EmptyInput() {
        // Arrange
        String authSubscriptionId = "";

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> transactionService.findByAuthSubscriptionId(authSubscriptionId),
                "Should throw EntityNotFoundException for empty input");
        assertEquals("Payment subscription not found", exception.getMessage());
        verify(transactionHistoryRepository, never()).findByAuthSubscriptionId(anyString());
    }

    @Test
    @DisplayName("Test findByAuthSubscriptionId NotFound")
    void testFindByAuthSubscriptionId_NotFound() throws EntityNotFoundException {
        // Arrange
        String authSubscriptionId = "AUTH999";

        when(transactionHistoryRepository.findByAuthSubscriptionId(authSubscriptionId)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> transactionService.findByAuthSubscriptionId(authSubscriptionId),
                "Should throw EntityNotFoundException if no transaction history is found");
        assertEquals("Payment subscription not found", exception.getMessage());
        verify(transactionHistoryRepository, times(1)).findByAuthSubscriptionId(authSubscriptionId);
    }

    @Test
    @DisplayName("Test findByLatestTransactionHistoryBySubsIdAndStatus Success")
    void testFindByLatestTransactionHistoryBySubsIdAndStatus_Success() throws EntityNotFoundException {
        TransactionHistory history = new TransactionHistory();
        when(transactionHistoryRepository.findFirstByStatusAndAuthSubscriptionIdOrderByCreationAtDesc(
                any(), any())).thenReturn(history);

        TransactionHistory result = transactionService
                .findByLatestTransactionHistoryBySubsIdAndStatus("32142143", GenericStatus.ACTIVE);

        assertNotNull(result);
        assertEquals(history, result);
        verify(transactionHistoryRepository)
                .findFirstByStatusAndAuthSubscriptionIdOrderByCreationAtDesc(any(), any());
    }

    @Test
    @DisplayName("Test findByLatestTransactionHistoryBySubsIdAndStatusAndSubscriptionStatus Success")
    void testFindByLatestTransactionHistoryBySubsIdAndStatusAndSubscriptionStatus_Success() throws EntityNotFoundException {
        TransactionHistory history = new TransactionHistory();
        when(transactionHistoryRepository
                .findFirstByStatusAndAuthSubscriptionIdAndSubscriptionStatusOrderByCreationAtDesc(
                        any(GenericStatus.class), any(String.class), any(SubscriptionStatus.class))).thenReturn(history);

        TransactionHistory result = transactionService
                .findByLatestTransactionHistoryBySubsIdAndStatusAndSubscriptionStatus("32142143", GenericStatus.ACTIVE, SubscriptionStatus.CONTINUE);

        assertNotNull(result);
        assertEquals(history, result);
        verify(transactionHistoryRepository)
                .findFirstByStatusAndAuthSubscriptionIdAndSubscriptionStatusOrderByCreationAtDesc(any(), any(), any());
    }

    @Test
    @DisplayName("Test findByLatestTransactionHistoryBySubsIdAndStatusAndSubscriptionStatus Throws EntityNotFoundException")
    void testFindByLatestTransactionHistoryBySubsIdAndStatusAndSubscriptionStatus_ThrowsEntityNotFoundException() {
        when(transactionHistoryRepository
                .findFirstByStatusAndAuthSubscriptionIdAndSubscriptionStatusOrderByCreationAtDesc(
                        any(), any(), any())).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                transactionService.findByLatestTransactionHistoryBySubsIdAndStatusAndSubscriptionStatus(any(), any(), any())
        );

        assertEquals("Payment subscription not found", exception.getMessage());
        verify(transactionHistoryRepository)
                .findFirstByStatusAndAuthSubscriptionIdAndSubscriptionStatusOrderByCreationAtDesc(any(), any(), any());
    }

    @Test
    @DisplayName("Test fetchAllTransactionHistoryBy Success")
    void testFetchAllTransactionHistoryBy_Success() throws EntityNotFoundException {
        List<TransactionHistory> histories = List.of(new TransactionHistory(), new TransactionHistory());

        when(transactionHistoryRepository.findAllBySubscriptionStatusOrStatusAndUser_Id(
                SubscriptionStatus.CONTINUE, GenericStatus.ACTIVE, 1L)).thenReturn(histories);

        List<TransactionHistory> result = transactionService.fetchAllTransactionHistoryBy(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(transactionHistoryRepository)
                .findAllBySubscriptionStatusOrStatusAndUser_Id(
                        SubscriptionStatus.CONTINUE, GenericStatus.ACTIVE, 1L);
    }

    @Test
    @DisplayName("Test fetchAllTransactionHistoryBy Throws EntityNotFoundException")
    void testFetchAllTransactionHistoryBy_ThrowsEntityNotFoundException() {
        when(transactionHistoryRepository.findAllBySubscriptionStatusOrStatusAndUser_Id(
                SubscriptionStatus.CONTINUE, GenericStatus.ACTIVE, 1L)).thenReturn(Collections.emptyList());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                transactionService.fetchAllTransactionHistoryBy(1L));

        assertEquals("No transaction history is present", exception.getMessage());
        verify(transactionHistoryRepository)
                .findAllBySubscriptionStatusOrStatusAndUser_Id(
                        SubscriptionStatus.CONTINUE, GenericStatus.ACTIVE, 1L);
    }

    @Test
    @DisplayName("Test fetchCurrentSubscription Success")
    void testFetchCurrentSubscription_Success() throws EntityNotFoundException {
        TransactionHistory history = new TransactionHistory();
        when(transactionHistoryRepository.fetchCurrentSubscription(1L))
                .thenReturn(Optional.of(history));

        TransactionHistory result = transactionService.fetchCurrentSubscription(1L);

        assertNotNull(result);
        assertEquals(history, result);
        verify(transactionHistoryRepository).fetchCurrentSubscription(1L);
    }

    @Test
    @DisplayName("Test fetchCurrentSubscription Throws EntityNotFoundException")
    void testFetchCurrentSubscription_ThrowsEntityNotFoundException() {
        when(transactionHistoryRepository.fetchCurrentSubscription(1L))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                transactionService.fetchCurrentSubscription(1L));

        assertEquals("No subscription present for this student.", exception.getMessage());
        verify(transactionHistoryRepository).fetchCurrentSubscription(1L);
    }

    @Test
    @DisplayName("Test findById Success")
    void testFindById_Success() throws EntityNotFoundException {
        Long transactionId = 10L;
        TransactionHistory history = new TransactionHistory();
        when(transactionHistoryRepository.findById(transactionId)).thenReturn(Optional.of(history));

        TransactionHistory result = transactionService.findById(transactionId);

        assertNotNull(result);
        assertEquals(history, result);
        verify(transactionHistoryRepository).findById(transactionId);
    }

    @Test
    @DisplayName("Test findById Throws EntityNotFoundException")
    void testFindById_NotFound_ThrowsException() {
        Long transactionId = 10L;
        when(transactionHistoryRepository.findById(transactionId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                transactionService.findById(transactionId));

        assertEquals("No transaction history found.", exception.getMessage());
        verify(transactionHistoryRepository).findById(transactionId);
    }

    @Test
    @DisplayName("Test findByLatestUserAndSubscriptionStatus Found")
    void testFindByLatestUserAndSubscriptionStatus_Found() {
        Long userId = 5L;
        SubscriptionStatus status = SubscriptionStatus.SUCCESS;
        TransactionHistory history = new TransactionHistory();
        when(transactionHistoryRepository.findFirstByUserIdAndSubscriptionStatusOrderByCreationAtDesc(userId, status))
                .thenReturn(history);

        TransactionHistory result = transactionService.findByLatestUserAndSubscriptionStatus(userId, status);

        assertNotNull(result);
        assertEquals(history, result);
        verify(transactionHistoryRepository).findFirstByUserIdAndSubscriptionStatusOrderByCreationAtDesc(userId, status);
    }

    @Test
    @DisplayName("Test findByLatestUserAndSubscriptionStatus Not Found")
    void testFindByLatestUserAndSubscriptionStatus_NotFound() {
        Long userId = 5L;
        SubscriptionStatus status = SubscriptionStatus.SUCCESS;
        when(transactionHistoryRepository.findFirstByUserIdAndSubscriptionStatusOrderByCreationAtDesc(userId, status))
                .thenReturn(null);

        TransactionHistory result = transactionService.findByLatestUserAndSubscriptionStatus(userId, status);

        assertNull(result);
        verify(transactionHistoryRepository).findFirstByUserIdAndSubscriptionStatusOrderByCreationAtDesc(userId, status);
    }

    @Test
    void testInactiveTransactionHistoryWhenCouponBasedSubscriptionApplied_WithExistingHistory() {
        User user = UserTestData.userData();
        SubscribedUser subscribedUser = SubscribedUserTestData.standardSubscribedUser();
        subscribedUser.setUser(user);
        Subscription subscription = SubscriptionTestData.standardSubscription();

        TransactionHistory existing = new TransactionHistory();
        existing.setId(99L);
        existing.setSubscriptionStatus(SubscriptionStatus.SUCCESS);
        existing.setStatus(GenericStatus.ACTIVE);

        when(transactionHistoryRepository
                .findFirstByUserIdAndSubscriptionStatusOrderByCreationAtDesc(user.getId(), SubscriptionStatus.SUCCESS))
                .thenReturn(existing);

        // capture saved objects
        ArgumentCaptor<TransactionHistory> captor = ArgumentCaptor.forClass(TransactionHistory.class);
        when(transactionHistoryRepository.save(any(TransactionHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.inactiveTransactionHistoryWhenCouponBasedSubscriptionApplied(
                "coupon applied", subscription, subscribedUser, user);

        verify(transactionHistoryRepository, times(2)).save(captor.capture());

        List<TransactionHistory> saved = captor.getAllValues();

        // Verify update of previous transaction
        TransactionHistory updated = saved.get(0);
        assertEquals(GenericStatus.INACTIVE, updated.getStatus());
        assertEquals(SubscriptionStatus.DISCONTINUE, updated.getSubscriptionStatus());

        // Verify new transaction
        TransactionHistory created = saved.get(1);
        assertEquals("FREE", created.getAuthSubscriptionId());
        assertEquals(0.0, created.getSubscriptionAmount());
        assertEquals(SubscriptionStatus.SUCCESS, created.getSubscriptionStatus());
        assertEquals(GenericStatus.ACTIVE, created.getStatus());
        assertEquals("coupon applied", created.getResponseText());
        assertEquals(existing.getId(), created.getOldTransactionId());
    }

    @Test
    void testInactiveTransactionHistoryWhenCouponBasedSubscriptionApplied_NoHistory() {
        User user = new User(); user.setId(1L);
        SubscribedUser subscribedUser = new SubscribedUser(); subscribedUser.setUser(user);
        Subscription subscription = new Subscription();

        // Simulate no history in SUCCESS, CONTINUE, or PENDING
        when(transactionHistoryRepository
                .findFirstByUserIdAndSubscriptionStatusOrderByCreationAtDesc(eq(user.getId()), any()))
                .thenReturn(null);

        when(transactionHistoryRepository.save(any(TransactionHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transactionService.inactiveTransactionHistoryWhenCouponBasedSubscriptionApplied(
                "no history", subscription, subscribedUser, user);

        // Only one save should be called for new transaction
        verify(transactionHistoryRepository, times(1)).save(any(TransactionHistory.class));
    }

}
