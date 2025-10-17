package com.example.todoapp.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ToDoAppESLEI2025@outlook.pt"); // remetente fixo
        message.setTo(to);                              // destinatário definido pelo utilizador
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
        System.out.println("✅ Email enviado para " + to);
    }
}
