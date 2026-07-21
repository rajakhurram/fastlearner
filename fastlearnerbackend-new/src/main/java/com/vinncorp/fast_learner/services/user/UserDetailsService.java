package com.vinncorp.fast_learner.services.user;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserDetails;
import com.vinncorp.fast_learner.repositories.user.UserDetailsRepository;
import com.vinncorp.fast_learner.util.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static org.springframework.data.elasticsearch.core.index.PutTemplateRequest.builder;

@Slf4j
@Service
public class UserDetailsService implements IUserDetailsService {

    private final UserDetailsRepository repository;
    private final IUserService userService;

    public UserDetailsService(UserDetailsRepository repository, IUserService userService) {
        this.repository = repository;
        this.userService= userService;
    }

    @Override
    public Message<UserDetails> getUserWelcomeStatus(String name) throws EntityNotFoundException {
        User user = userService.findByEmail(name);
        if (Objects.isNull(user)) {
            log.error("User not found for email: {}", name);
            throw new EntityNotFoundException("User not found with email: " + name);
        }

        Optional<UserDetails> userDetails = repository.findByUser(user);
        UserDetails userDetail = null ;
        if(userDetails.isPresent()) {
            userDetail = userDetails.get();
        }

        else{
            userDetail = new UserDetails(); // Don't forget to instantiate!
          userDetail.setUser(user);
          userDetail.setWelcomeInstructorDashboard(true);
          userDetail =  repository.save(userDetail);

          userDetail.setWelcomeInstructorDashboard(false);
        }

        return new Message<UserDetails>()
                .setData(userDetail)
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("User Welcome Status Found Successfully ");

    }

}
