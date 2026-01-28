package com.vinncorp.fast_learner.mock.contact_us;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.contact_us.ContactUs;
import com.vinncorp.fast_learner.repositories.contact_us.ContactUsRepository;
import com.vinncorp.fast_learner.request.contact_us.ContactUsRequest;
import com.vinncorp.fast_learner.services.contact_us.ContactUsService;
import com.vinncorp.fast_learner.services.email_template.EmailService;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ContactUsServiceTest {

    @Mock
    private ContactUsRepository repo;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ContactUsService contactUsService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Successfully submit contact us request")
    public void testSubmission_Success() throws InternalServerException {
        ContactUsRequest request = ContactUsTestData.getContactUsRequest();

        Message<String> response = contactUsService.submission(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Your request has been submitted.", response.getMessage());

        verify(repo, times(1)).save(any(ContactUs.class));
        verify(emailService, times(1)).sendEmail(
                eq(null),
                eq("Contact US Form Submission"),
                any(String.class),
                eq(true)
        );
    }

    @Test
    @DisplayName("Throw InternalServerException when repo.save throws exception")
    public void testSubmission_InternalServerException() {
        ContactUsRequest request = new ContactUsRequest();
        request.setFullName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("1234567890");
        request.setDescription("Need assistance with my account.");

        doThrow(new RuntimeException("Database error")).when(repo).save(any(ContactUs.class));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            contactUsService.submission(request);
        });

        assertEquals("Contact us" + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
    }
}
