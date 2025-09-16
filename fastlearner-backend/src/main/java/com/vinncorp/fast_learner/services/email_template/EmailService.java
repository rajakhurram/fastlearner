package com.vinncorp.fast_learner.services.email_template;

import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.Constants.EmailTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    @Value("${frontend.domain.url}")
    private String FRONTEND_DOMAIN_URL;

    @Value("${frontend.reset.password.url}")
    private String FRONTEND_RESET_PASSWORD_URL;

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String to, String subject, String text, boolean isHtml) {
        MimeMessage mailMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, isHtml);
            javaMailSender.send(mailMessage);
            log.info("Mail sent successfully.");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendOtpEmailForResettingPassword(User user, int otp) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.setRecipients(MimeMessage.RecipientType.TO, user.getEmail());

        message.setContent(EmailTemplate.FIRST_PART_RESET_PASSWORD_TEMPLATE + FRONTEND_DOMAIN_URL + FRONTEND_RESET_PASSWORD_URL +
                "otp=" + otp + "&email=" + user.getEmail() + EmailTemplate.LAST_PART_RESET_PASSWORD_TEMPLATE, "text/html; charset=utf-8");
        message.setSubject("Resetting password.");

        javaMailSender.send(message);
    }
}
