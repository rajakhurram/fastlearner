package com.vinncorp.fast_learner.mock.email_template;

import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.email_template.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailService, "FRONTEND_DOMAIN_URL", "http://localhost:4200");
        ReflectionTestUtils.setField(emailService, "FRONTEND_RESET_PASSWORD_URL", "/auth/reset-password?");
    }

    @Test
    @DisplayName("Test: Successfully send an email")
    void testSendEmail_Success() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail("test@example.com", "Test Subject", "Test Text", true);

        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Test: Handle MessagingException during email sending")
    void testSendEmail_MessagingException() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Email sending failed")).when(javaMailSender).send(mimeMessage);

        assertThrows(RuntimeException.class, () -> emailService.sendEmail("test@example.com", "Test Subject", "Test Text", true));
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Test: Successfully send an OTP email for resetting password")
    void testSendOtpEmailForResettingPassword_Success() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        User user = UserTestData.userData();

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendOtpEmailForResettingPassword(user, 123456);

        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Test: Handle MessagingException during OTP email sending")
    void testSendOtpEmailForResettingPassword_MessagingException() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        User user = UserTestData.userData();

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("OTP Email sending failed")).when(javaMailSender).send(mimeMessage);

        assertThrows(RuntimeException.class, () -> emailService.sendOtpEmailForResettingPassword(user, 123456));
        verify(javaMailSender, times(1)).send(mimeMessage);
    }
}
