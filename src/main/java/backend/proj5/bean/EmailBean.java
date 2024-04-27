package backend.proj5.bean;

import backend.proj5.dto.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.Properties;

@Stateless
public class EmailBean {

    @EJB
    private UserBean userBean;

    private static final Logger logger = LogManager.getLogger(TaskBean.class);
    private final String username = "pedro_domingos10@hotmail.com";
    private final String password = System.getenv("SMTP_PASSWORD");
    private final String host = "smtp.office365.com"; //"smtp-mail.outlook.com" estava assim definido primeiro e começou a dar erro
    private final int port = 587;

    public EmailBean() {}

    public boolean sendEmail(String to, String subject, String body) {
        logger.info("Sending email to: {}", to);
        boolean sent = false;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                logger.info("Authenticating email");
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            sent = true;

            logger.info("Email sent to: {}", to);

        } catch (MessagingException e) {
            sent = false;
            e.printStackTrace();
            logger.error("Email not sent to: {}", to);
        }

        return sent;
    }

    public boolean sendConfirmationEmail(User user, String validationToken) {
        logger.info("Sending confirmation email to: {}", user.getEmail());

        boolean sent = false;

        String userEmail = user.getEmail();
        String subject = "Agile Scrum - Account Confirmation";
        String confirmationLink = "http://localhost:5173/confirm/" + validationToken;
        String body = "Dear " + user.getFirstName() + ",\n\n"
                + "Thank you for registering with us. Please click on the link below to confirm your account.\n\n"
                + "Confirmation Link: " + confirmationLink;

        if (sendEmail(userEmail, subject, body)) {
            sent = true;
            logger.info("Confirmation email sent to: {}", userEmail);
        } else {
            userBean.delete(user.getUsername());
            logger.error("Confirmation email not sent to: {}", userEmail);
        }
        return sent;
    }

    public boolean sendPasswordResetEmail(String email, String firstName, String validationToken) {
        logger.info("Sending password reset email to: {}", email);
        boolean sent = false;

        String subject = "Agile Scrum - Password Reset";
        String resetLink = "http://localhost:5173/reset-password/" + validationToken;
        String body = "Dear " + firstName + ",\n\n"
                + "Please click on the link below to reset your password.\n\n"
                + "Reset Link: " + resetLink;

        if (sendEmail(email, subject, body)) {
            sent = true;
            logger.info("Password reset email sent to: {}", email);
        } else {
            logger.error("Password reset email not sent to: {}", email);
        }

        return sent;
    }
}
