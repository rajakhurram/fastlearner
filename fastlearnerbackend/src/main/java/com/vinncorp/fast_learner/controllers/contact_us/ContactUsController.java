package com.vinncorp.fast_learner.controllers.contact_us;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.contact_us.ContactUsRequest;
import com.vinncorp.fast_learner.services.contact_us.IContactUsService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(APIUrls.CONTACT_US)
@RequiredArgsConstructor
public class ContactUsController {

    private final IContactUsService service;

    @PostMapping(APIUrls.SUBMIT)
    public ResponseEntity<Message<String>> submission(@Valid @RequestBody ContactUsRequest request)
            throws InternalServerException {
        var m = service.submission(request);
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}