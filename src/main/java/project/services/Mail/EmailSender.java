package project.services.Mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class EmailSender {

    @Value("${webPortal.link}")
    private String webPortalLink;

    private final JavaMailSender emailSender;

    public EmailSender(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public SimpleMailMessage templateResetTokenMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText("To reset your password please click the link below:\n%s\n" + // pod %s zostanie wstawiony token
                "If this message was not sent at your request - just ignore it. Your password will not be changed.");
        return message;
    }

    public SimpleMailMessage templateActivationCodeMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText("Confirm your email address!\n" +
                "You are only one step away from creating the account. To start using Virtual Stock Exchange, enter the verification code:\n%s\n"
                + "The verification code expires after 2 hours.\nThank you, Virtual Stock Exchange"
        );
        return message;
    }

    public String generateLinkToReset(String token) {
        return webPortalLink + "/reset-password/" + token;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
