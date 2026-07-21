package com.vinncorp.fast_learner.controllers.transaction_history;


import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.response.transaction_history.TransactionHistoryResponse;
import com.vinncorp.fast_learner.services.transaction_history.ITransactionHistoryService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.TRANSACTIONS_HISTORY)
@RequiredArgsConstructor
public class TransactionHistoryController {
    private final ITransactionHistoryService transactionHistoryService;
    private final IUserService userService;

    @GetMapping(APIUrls.GET_BY_USER)
    public ResponseEntity<Message<Page<TransactionHistoryResponse>>> getAllTransactionHistory(
            @RequestParam(required = false, defaultValue = "0") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            Principal principal)
            throws EntityNotFoundException {

        Message<Page<TransactionHistoryResponse>> m = transactionHistoryService.fetchTransactionHistory(PageRequest.of(pageNo, pageSize), principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }


    @GetMapping(APIUrls.GET_BY_ID)
    public ResponseEntity<byte[]> getTransactionById(@Param("transactionId")Long transactionId, Principal principal)
            throws EntityNotFoundException {
        return transactionHistoryService.downloadInvoiceByTransactionId(transactionId);
    }

    @GetMapping(APIUrls.DOWNLOAD_INVOICE)
    public ResponseEntity<byte[]> downloadInvoiceByTransactionId(
            @Param("transactionId") Long transactionId,
            Principal principal) throws EntityNotFoundException {

        return transactionHistoryService.downloadInvoiceByTransactionId(transactionId);
    }
}
