package com.vinncorp.fast_learner.services.transaction_history;

import com.itextpdf.text.DocumentException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.response.transaction_history.TransactionHistoryResponse;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.template.InvoiceTemplate;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import org.springframework.core.io.ResourceLoader;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.tansaction_history.TransactionHistoryRepository;

import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TransactionHistoryService implements ITransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final IUserService userService;
    private final ISubscriptionService subscriptionService;

    private final ResourceLoader resourceLoader;

    public TransactionHistoryService(TransactionHistoryRepository transactionHistoryRepository, @Lazy IUserService userService,
                                     @Lazy ISubscriptionService subscriptionService, ResourceLoader resourceLoader) {
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public TransactionHistory findByAuthSubscriptionId(String paymentSubscriptionId) throws EntityNotFoundException {
        if (paymentSubscriptionId != null && !paymentSubscriptionId.isEmpty()) {
            TransactionHistory transactionHistory = transactionHistoryRepository.findByAuthSubscriptionId(paymentSubscriptionId);
            if (transactionHistory == null) {
                throw new EntityNotFoundException("Payment subscription not found");
            }
            return transactionHistory;
        } else {
            throw new EntityNotFoundException("Payment subscription not found");
        }
    }

    @Override
    public TransactionHistory findByLatestTransactionHistoryBySubsIdAndStatus(String paymentSubscriptionId, GenericStatus genericStatus) throws EntityNotFoundException {
        return transactionHistoryRepository.findFirstByStatusAndAuthSubscriptionIdOrderByCreationAtDesc(
                genericStatus.name(),
                paymentSubscriptionId);
    }

    @Override
    public TransactionHistory findByLatestTransactionHistoryBySubsIdAndStatusAndSubscriptionStatus(String paymentSubscriptionId,
                                                                                                   GenericStatus genericStatus,
                                                                                                   SubscriptionStatus subscriptionStatus)
            throws EntityNotFoundException {
        TransactionHistory transactionHistory = transactionHistoryRepository
                .findFirstByStatusAndAuthSubscriptionIdAndSubscriptionStatusOrderByCreationAtDesc(
                        genericStatus, paymentSubscriptionId, subscriptionStatus);
        if (transactionHistory == null) {
            throw new EntityNotFoundException("Payment subscription not found");
        }
        return transactionHistory;
    }

    @Override
    @Transactional
    public TransactionHistory save(TransactionHistory transactionHistory) {
        return transactionHistoryRepository.save(transactionHistory);
    }

    @Override
    public Message<Page<TransactionHistoryResponse>> fetchTransactionHistory(Pageable pageable, String email) throws EntityNotFoundException {
        log.info("Fetching transaction history for email: {}", email);
            User user=userService.findByEmail(email);
            if (user!=null){

            log.info("User fetched successfully: {}", user.getId());
            Page<TransactionHistoryResponse> transactionHistory=transactionHistoryRepository.findByUserOrderByIdDesc(user.getId(), pageable);
                log.info("Transaction history fetched, records count: {}",
                        (transactionHistory != null) ? transactionHistory.getContent().size() : 0);
                if (!transactionHistory.getContent().isEmpty()){
//                transactionHistory= transactionHistory.getContent().stream().map(TransactionHistory::from).collect(Collectors.toList());
                log.info("Transaction history transformed successfully.");
                return new Message<Page<TransactionHistoryResponse>>()
                        .setStatus(HttpStatus.OK.value())
                        .setCode(HttpStatus.OK.name())
                        .setMessage("Transaction history fetched successfully.")
                        .setData(transactionHistory);
            }else {
                log.warn("No transaction history found for user: {}", user.getId());
                throw new EntityNotFoundException("Transaction history not found");
            }
        }else {
                log.warn("User not found");
            throw new EntityNotFoundException("User not found");
        }
    }

    @Override
    public Message<TransactionHistory> getByTransactionId(Long transactionId) throws EntityNotFoundException {
        if (Objects.isNull(transactionId)) {
            throw new EntityNotFoundException("transaction id should not be null");
        }
        TransactionHistory transactionHistory = transactionHistoryRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction history not found."));
        return new Message<TransactionHistory>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Transaction history fetched successfully.")
                .setData(transactionHistory);
    }

    @Override
    public List<TransactionHistory> fetchAllTransactionHistoryBy(long studentId) throws EntityNotFoundException {
        log.info("Fetching all transaction history for student ID: " + studentId);
        List<TransactionHistory> transactionHistories = transactionHistoryRepository.findAllBySubscriptionStatusOrStatusAndUser_Id(
                SubscriptionStatus.CONTINUE, GenericStatus.ACTIVE, studentId);
        if(CollectionUtils.isEmpty(transactionHistories))
            throw new EntityNotFoundException("No transaction history is present");
        return transactionHistories;
    }

    @Override
    public TransactionHistory fetchCurrentSubscription(Long studentId) throws EntityNotFoundException {
        log.info("Fetching current subscription by student: " + studentId);

        return transactionHistoryRepository.fetchCurrentSubscription(studentId)
                .orElseThrow(() -> new EntityNotFoundException("No subscription present for this student."));
    }

    @Override
    public ResponseEntity<byte[]> downloadInvoiceByTransactionId(Long transactionId) throws EntityNotFoundException {
        if (transactionId == null) {
            throw new EntityNotFoundException("Transaction ID should not be null");
        }

        // Fetch transaction history
        TransactionHistory transactionHistory = transactionHistoryRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction history not found"));

        Double finalAmount=0.0;
        Double discount= 0.0;
        if (transactionHistory.getCoupon() != null) {
            Double discountPercentage = transactionHistory.getCoupon().getDiscount();
            discount = transactionHistory.getSubscriptionAmount() * (discountPercentage / 100.0); //
            finalAmount = transactionHistory.getSubscriptionAmount() - discount;
        } else {
            finalAmount = transactionHistory.getSubscriptionAmount();
        }

        boolean plan = true;
        try {
            // Generate HTML content using InvoiceTemplate
            String htmlContent = InvoiceTemplate.generateInvoiceTemplate(
                    transactionHistory.getSubscription().getName(),
                    transactionHistory.getUser().getEmail(),
                    transactionHistory.getUser().getFullName(),
                    transactionHistory.getSubscriptionAmount(),
                    discount,
                    finalAmount,
                    transactionHistory.getCreationAt(),
                    transactionHistory.getAuthSubscriptionId(),
                    plan
            );

            // Generate PDF from HTML
            ByteArrayOutputStream pdfStream = generatePdfFromHtml(htmlContent);

            // Create the final ResponseEntity
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + transactionId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfStream.toByteArray()); // Ensure .body() is used here
        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice PDF", e);
        }
    }

    @Override
    public TransactionHistory findByLatestTransactionHistoryByUserIdAndStatus(Long userId, GenericStatus status) throws EntityNotFoundException {
        TransactionHistory transactionHistory = transactionHistoryRepository
                .findFirstByStatusAndUserIdOrderByCreationAtDesc(
                        status, userId);
        if (transactionHistory == null) {
            return null;
//            throw new EntityNotFoundException("Payment subscription not found");
        }
        return transactionHistory;
    }

    @Override
    public TransactionHistory findById(Long oldTransactionId) throws EntityNotFoundException {
        log.info("Fetching transaction history by transaction id: {}", oldTransactionId);

        return transactionHistoryRepository.findById(oldTransactionId)
                .orElseThrow(() -> new EntityNotFoundException("No transaction history found."));
    }

    @Override
    public TransactionHistory findByLatestUserAndSubscriptionStatus(Long userId, SubscriptionStatus subscriptionStatus) {
        log.info("Transaction history finding by subscription status and user");
        TransactionHistory transactionHistory=transactionHistoryRepository.findFirstByUserIdAndSubscriptionStatusOrderByCreationAtDesc(userId,subscriptionStatus);
        if (transactionHistory==null){
            log.info("transaction history not found");
            return null;
        }
        log.info("transaction history found");

        return transactionHistory;
    }

    @Override
    public void inactiveTransactionHistoryWhenCouponBasedSubscriptionApplied(String responseText, Subscription subscription, SubscribedUser subscribedUser, User user) {
        log.info("Update all transaction history when coupon based subscription is applied.");
        TransactionHistory continueTransactionHistory = null;
        if(Objects.nonNull(subscribedUser)) {
            continueTransactionHistory = Optional.ofNullable(
                            findByLatestUserAndSubscriptionStatus(subscribedUser.getUser().getId(), SubscriptionStatus.SUCCESS))
                    .orElseGet(() -> Optional.ofNullable(findByLatestUserAndSubscriptionStatus(subscribedUser.getUser().getId(), SubscriptionStatus.CONTINUE))
                                .orElseGet(() -> findByLatestUserAndSubscriptionStatus(subscribedUser.getUser().getId(), SubscriptionStatus.PENDING)));

            if (continueTransactionHistory != null) {
                continueTransactionHistory.setUpdatedDate(new Date());
                continueTransactionHistory.setStatus(GenericStatus.INACTIVE);
                continueTransactionHistory.setSubscriptionStatus(SubscriptionStatus.DISCONTINUE);
                save(continueTransactionHistory);
            }
            log.info("Creating the transaction history for user: {}", subscribedUser.getUser().getId());
        }

        TransactionHistory transactionHistory = TransactionHistory.builder()
                .subscription(subscription)
                .authSubscriptionId("FREE")
                .user(user)
                .subscriptionStatus(SubscriptionStatus.SUCCESS)
                .subscriptionAmount(0.0)
                .status(GenericStatus.ACTIVE)
                .creationAt(new Date())
                .responseCode("OK")
                .responseText(responseText)
                .oldTransactionId(continueTransactionHistory != null ? continueTransactionHistory.getId() : null)
                .build();
        save(transactionHistory);
    }

    private ByteArrayOutputStream generatePdfFromHtml(String htmlContent) throws DocumentException, com.lowagie.text.DocumentException {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(pdfStream);
        return pdfStream;
    }
}

