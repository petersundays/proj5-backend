package backend.proj5.bean;

import backend.proj5.dto.User;
import jakarta.ejb.Stateless;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


import java.util.Properties;

@Stateless
public class EmailBean {

    private final String username = "pedro_domingos10@hotmail.com";
    private final String password = System.getenv("SMTP_PASSWORD");
    private final String host = "smtp-mail.outlook.com";
    private final int port = 587;

    public EmailBean() {}

    public boolean sendEmail(String to, String subject, String body) {
        boolean sent = false;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
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
        } catch (MessagingException e) {
            sent = false;
            e.printStackTrace();
        }

        return sent;
    }

    public boolean sendConfirmationEmail(User user) {

        System.out.println("Sending email to " + user.getEmail());

        String userEmail = user.getEmail();
        String subject = "Agile Scrum - Account Confirmation";
        String confirmationLink = "http://example.com/confirm-account?email=" + userEmail;
        String body = "Dear " + user.getFirstName() + ",\n\n"
                + "Thank you for registering with us. Please click on the link below to confirm your account.\n\n"
                + "Confirmation Link: " + confirmationLink;

        return sendEmail(userEmail, subject, body);
    }
}
