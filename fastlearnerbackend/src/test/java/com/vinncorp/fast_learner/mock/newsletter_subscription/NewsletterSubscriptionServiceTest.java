package com.vinncorp.fast_learner.mock.newsletter_subscription;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityAlreadyExistException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.newsletter_subscription.NewsletterSubscription;
import com.vinncorp.fast_learner.repositories.newsletter_subscription.NewsletterSubscriptionRepository;
import com.vinncorp.fast_learner.services.newsletter_subscription.NewsletterSubscriptionSrvice;
import com.vinncorp.fast_learner.util.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NewsletterSubscriptionServiceTest {

    @Mock
    private NewsletterSubscriptionRepository repo;

    @InjectMocks
    private NewsletterSubscriptionSrvice service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test subscribeToNewsletter - Successful subscription")
    void testSubscribeToNewsletter_Success() throws InternalServerException, EntityAlreadyExistException, BadRequestException {
        String email = "test@Email.com";

        when(repo.existsByEmail(email.trim().toLowerCase())).thenReturn(false);
        when(repo.save(any(NewsletterSubscription.class))).thenReturn(null);

        Message<String> response = service.subscribeToNewsletter(email);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(HttpStatus.OK.name(), response.getCode());
        assertEquals("Successfully subscribed the newsletter.", response.getMessage());

        verify(repo, times(1)).save(any(NewsletterSubscription.class));
    }

    @Test
    @DisplayName("Test subscribeToNewsletter - Email already subscribed")
    void testSubscribeToNewsletter_EmailAlreadySubscribed() {
        String email = "test@Email.com";
        when(repo.existsByEmail(email.trim().toLowerCase())).thenReturn(true);

        EntityAlreadyExistException exception = assertThrows(EntityAlreadyExistException.class, () -> {
            service.subscribeToNewsletter(email);
        });

        assertEquals("Newsletter already subscribed by this email: " + email, exception.getMessage());
        verify(repo, never()).save(any(NewsletterSubscription.class));
    }

    @Test
    @DisplayName("Test subscribeToNewsletter - Repository save fails")
    void testSubscribeToNewsletter_SaveFails() {
        String email = "test@Email.com";

        when(repo.existsByEmail(email.trim().toLowerCase())).thenReturn(false);
        when(repo.save(any(NewsletterSubscription.class))).thenThrow(new RuntimeException("Database error"));

        InternalServerException exception = assertThrows(InternalServerException.class, () -> {
            service.subscribeToNewsletter(email);
        });

        assertEquals("Newsletter subscription " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR, exception.getMessage());
    }
}
