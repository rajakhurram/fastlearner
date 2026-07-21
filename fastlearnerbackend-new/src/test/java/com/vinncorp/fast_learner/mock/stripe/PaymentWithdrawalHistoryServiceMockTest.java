package com.vinncorp.fast_learner.mock.stripe;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.stripe.PaymentWithdrawalHistory;
import com.vinncorp.fast_learner.repositories.stripe.PaymentWithdrawalHistoryRepository;
import com.vinncorp.fast_learner.response.stripe.PaymentWithdrawalHistoryResponse;
import com.vinncorp.fast_learner.dtos.stripe.PaymentWithdrawalHistoryDTO;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.stripe.PaymentWithdrawalHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

class PaymentWithdrawalHistoryServiceMockTest {

    @Mock
    private IUserService userService;

    @Mock
    private PaymentWithdrawalHistoryRepository repo;

    @InjectMocks
    private PaymentWithdrawalHistoryService paymentWithdrawalHistoryService;

    private User mockUser;
    private PaymentWithdrawalHistory mockHistory;
    private List<PaymentWithdrawalHistory> mockHistoryList;
    private PaymentWithdrawalHistoryResponse mockHistoryResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock user setup
        mockUser = UserTestData.userData();

        // Mock PaymentWithdrawalHistory setup
        mockHistory = new PaymentWithdrawalHistory();
        mockHistory.setId(1L);
        mockHistory.setAmount(100.00);
        mockHistory.setBankName("Test Bank");
        mockHistory.setWithdrawalAt(new Date());

        mockHistoryList = List.of(mockHistory);

        // Mock PaymentWithdrawalHistoryResponse setup
        var pymntWithdrwlHstryDTO = PaymentWithdrawalHistoryDTO.builder()
                .id(mockHistory.getId())
                .amount(mockHistory.getAmount())
                .bankName(mockHistory.getBankName())
                .withdrawalAt(mockHistory.getWithdrawalAt())
                .build();
        mockHistoryResponse = PaymentWithdrawalHistoryResponse.builder()
                .histories(Arrays.asList(pymntWithdrwlHstryDTO))
                .pageNo(0)
                .pageSize(5)
                .totalPages(1)
                .totalElements(1)
                .build();
    }

    @Test
    void testFetchConnectedAccountHistory_success() throws EntityNotFoundException, EntityNotFoundException {
        // Arrange
        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        Page<PaymentWithdrawalHistory> page = new PageImpl<>(mockHistoryList);
        when(repo.findByUserId(eq(mockUser.getId()), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        Message<PaymentWithdrawalHistoryResponse> response = paymentWithdrawalHistoryService
                .fetchConnectedAccountHistory(mockUser.getEmail(), 0, 5);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Payment withdrawn history successfully fetched.", response.getMessage());
        assertEquals(1, response.getData().getHistories().size());
    }

    @Test
    void testFetchConnectedAccountHistory_notFound() throws EntityNotFoundException {
        // Arrange
        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        Page<PaymentWithdrawalHistory> emptyPage = new PageImpl<>(List.of());
        when(repo.findByUserId(eq(mockUser.getId()), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                paymentWithdrawalHistoryService.fetchConnectedAccountHistory(mockUser.getEmail(), 0, 5));
    }

    @Test
    void testSave_success() throws InternalServerException {
        // Arrange
        when(repo.save(any(PaymentWithdrawalHistory.class))).thenReturn(mockHistory);

        // Act
        paymentWithdrawalHistoryService.save(mockHistory);

        // Assert
        verify(repo, times(1)).save(mockHistory);
    }

    @Test
    void testSave_throwsInternalServerException() {
        // Arrange
        doThrow(new RuntimeException("Error saving")).when(repo).save(any(PaymentWithdrawalHistory.class));

        // Act & Assert
        assertThrows(InternalServerException.class, () ->
                paymentWithdrawalHistoryService.save(mockHistory));
    }
}

