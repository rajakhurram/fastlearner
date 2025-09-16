package com.vinncorp.fast_learner.controllers.payout;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.services.payout.IPayoutWatchTimeService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.PAYOUT_WATCH_TIME)
@RequiredArgsConstructor
public class PayoutWatchTimeController {

    private final IPayoutWatchTimeService service;

    @PostMapping(APIUrls.CREATE_PAYOUT_WATCH_TIME)
    public ResponseEntity<Message> create(@RequestParam Long courseId, @RequestParam long watchTime, Principal principal)
            throws EntityNotFoundException, BadRequestException, InternalServerException {
        var m = service.create(courseId, watchTime, principal.getName());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
