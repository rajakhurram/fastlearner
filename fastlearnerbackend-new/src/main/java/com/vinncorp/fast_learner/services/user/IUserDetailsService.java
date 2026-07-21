package com.vinncorp.fast_learner.services.user;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.user.UserDetails;
import com.vinncorp.fast_learner.util.Message;

public interface IUserDetailsService {


    Message<UserDetails> getUserWelcomeStatus(String name) throws EntityNotFoundException;
}
