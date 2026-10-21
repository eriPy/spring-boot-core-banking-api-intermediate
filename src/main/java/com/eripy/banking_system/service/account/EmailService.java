package com.eripy.banking_system.service.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.exceptions.MailDeliveryException;

@Service 
public class EmailService {
    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(
        JavaMailSender mailSender
    ) {
        this.mailSender = mailSender;
    }

    public void sendEmailToSignUp(String to, String sdt) {
        logger.info("Preparing an email");
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("bank-account@gmail.com");  
        msg.setTo(to);
        msg.setSubject("Gmail Authentication");
        msg.setText("Token to Sign Up: " + sdt);
        try {
            logger.info("Sending email to user");
            mailSender.send(msg);
            logger.info("The email was send to user");
        } catch (MailAuthenticationException e) {
            logger.warn("Invalid credentials, info: " + e.getMessage());
            throw new MailDeliveryException("Problem in the client service");
        } catch (MailSendException e) {
            logger.warn("Cannot send the email to: " + to + ", info: " + e.getMessage());
            throw new MailDeliveryException("Cannot send the email");
        }
    }

    public void sendEmail(String to, String subject, String text) {
        logger.info("Preparing an email");
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("bank-account@gmail.com");  
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        try {
            logger.info("Sending email to user");
            mailSender.send(msg);
            logger.info("The email was send to user");
        } catch (MailAuthenticationException e) {
            logger.warn("Invalid credentials, info: " + e.getMessage());
            throw new MailDeliveryException("Problem in the client service");
        } catch (MailSendException e) {
            logger.warn("Cannot send the email to: " + to + ", info: " + e.getMessage());
            throw new MailDeliveryException("Cannot send the email");
        }
    }
}
