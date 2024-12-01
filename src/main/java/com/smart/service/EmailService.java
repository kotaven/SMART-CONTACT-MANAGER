package com.smart.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import java.util.Properties;

@Service
public class EmailService {

    public boolean sendEmail(String subject, String message, String to) {
        boolean f = false;
        String from = "kotavenkatesh2618@gmail.com";
        
        // SMTP server configuration for Gmail
        String host = "smtp.gmail.com";
        
        // Set properties for Gmail SMTP
        Properties properties = System.getProperties();
        System.out.println("PROPERTIES: " + properties);
        
        
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth",  "true");
        

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("kotavenkatesh2618@gmail.com", "oean rjfp iunp fdkv");
            }
        });
        

        session.setDebug(true);
        
        MimeMessage m = new MimeMessage(session);

        try {

            m.setFrom(from);
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            m.setSubject(subject);
           // m.setText(message);
            m.setContent(message,"text/html");

          
            Transport.send(m);
            System.out.println("Email sent successfully.");
            f=true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return f;
    }
}
