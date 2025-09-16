package com.vinncorp.fast_learner.services.contact_us;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.contact_us.ContactUsRepository;
import com.vinncorp.fast_learner.models.contact_us.ContactUs;
import com.vinncorp.fast_learner.request.contact_us.ContactUsRequest;
import com.vinncorp.fast_learner.services.email_template.IEmailService;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactUsService implements IContactUsService{
    private static final String DEFAULT_SUPPORT_EMAIL = "support-fastlearner@mailinator.com";

    @Value("${support.email:" + DEFAULT_SUPPORT_EMAIL + "}")
    private String SUPPORT_EMAIL;

    private final ContactUsRepository repo;

    private final IEmailService emailService;

    @Override
    public Message<String> submission(ContactUsRequest request) throws InternalServerException {
        log.info("Submitting the request of a user: " + request.getFullName());
        ContactUs contactUs = ContactUs.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .description(request.getDescription())
                .build();

        try {
            repo.save(contactUs);
        } catch (Exception e) {
            throw new InternalServerException("Contact us"+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }

        // Send email of the users' contact us submission
        String body = "<h3>Name: " + request.getFullName() + "</h3><br/><h4>Email: " + request.getEmail() +
                "</h4><br/><h4>Phone No: " + request.getPhoneNumber() + "</h4><br/><p><b>Description:</b> " +
                request.getDescription() + "</p><br/>";
        emailService.sendEmail(SUPPORT_EMAIL, "Contact US Form Submission", body, true);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setData("Your request has been submitted.")
                .setMessage("Your request has been submitted.");
    }
}
