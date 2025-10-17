/*package com.example.todoapp.views;

import com.example.todoapp.util.EmailService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
public class EmailView extends VerticalLayout {

    @Autowired
    private EmailService emailService;

    public EmailView() {
        TextField destinatario = new TextField("Destinatário");
        TextField assunto = new TextField("Assunto");
        TextArea corpo = new TextArea("Mensagem");

        Button enviar = new Button("Enviar Email", e -> {
            try {
                emailService.sendSimpleEmail(
                        destinatario.getValue(),
                        assunto.getValue(),
                        corpo.getValue()
                );
                Notification.show("Email enviado com sucesso!", 4000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Erro ao enviar email: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE);
            }
        });

        add(destinatario, assunto, corpo, enviar);
    }
}*/
