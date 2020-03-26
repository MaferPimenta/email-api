package com.email.Controllers;

import com.email.Models.Attachment;
import com.email.Models.Mail;
import com.email.Services.AttachmentService;
import com.email.Utils.HTMLDataSource;
import com.sun.mail.smtp.SMTPTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Controller
public class MailController {

    @Autowired
    AttachmentService attachmentService;

    @Value("${mail.smtp.host}")
    private String host;

    @Value("${mail.smtp.port}")
    private String port;

    @Value("${mail.smtp.user}")
    private String user;

    @Value("${mail.smtp.password}")
    private String password;

    @RequestMapping(path =  "/send", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> sendMessage(@RequestBody Mail mail) {
        Properties prop = System.getProperties();
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", port);
        prop.put("mail.smtp.user", user);
        prop.put("mail.smtp.password", password);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        javax.mail.Authenticator auth = new javax.mail.Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };

        Session session = Session.getInstance(prop, auth);
        Message message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(mail.getFrom()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.getTo(), false));

            if (mail.getCc() != null) {
                message.setReplyTo(InternetAddress.parse(mail.getCc()));
            }

            message.setSubject(mail.getSubject());
            MimeBodyPart body = new MimeBodyPart();
            body.setDataHandler(new DataHandler(new HTMLDataSource(mail.getBody())));

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(body);

            if (mail.getAttachments() != null) {
                for (Attachment attachment : mail.getAttachments()) {
                    attachmentService.convert(attachment.getFilename(), attachment.getBase64());

                    MimeBodyPart file = new MimeBodyPart();
                    FileDataSource fileDataSource = new FileDataSource("temp/" + attachment.getFilename());
                    file.setDataHandler(new DataHandler(fileDataSource));
                    file.setFileName(fileDataSource.getName());

                    multipart.addBodyPart(file);
                }
            }

            message.setContent(multipart);

            SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            try {
                if (mail.getAttachments() != null) {
                    for (Attachment attachment : mail.getAttachments()) {
                        attachmentService.deleteFile(attachment.getFilename());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (MessagingException e) {
            e.printStackTrace();

            Map<String, String> errorBody = new LinkedHashMap<>();
            errorBody.put("message", e.getMessage());

            return new ResponseEntity<>(errorBody, HttpStatus.BAD_REQUEST);
        }
    }
}
