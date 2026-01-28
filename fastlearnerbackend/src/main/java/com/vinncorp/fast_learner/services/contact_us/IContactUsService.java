package com.vinncorp.fast_learner.services.contact_us;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.contact_us.ContactUsRequest;
import com.vinncorp.fast_learner.util.Message;

public interface IContactUsService {
    Message<String> submission(ContactUsRequest contactUs) throws InternalServerException;
}
