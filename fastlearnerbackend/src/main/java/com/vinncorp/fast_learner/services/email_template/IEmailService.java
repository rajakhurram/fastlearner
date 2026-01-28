package com.vinncorp.fast_learner.services.email_template;


import com.vinncorp.fast_learner.models.user.User;
import jakarta.mail.MessagingException;

public interface IEmailService {
    void sendEmail(String to, String subject, String text, boolean isHtml);

    void sendOtpEmailForResettingPassword(User user, int otp) throws MessagingException;



}
