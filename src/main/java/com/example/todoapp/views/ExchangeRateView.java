package com.example.todoapp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.example.todoapp.util.ExchangeRateService;

@Route("exchange")
public class ExchangeRateView extends VerticalLayout {

    public ExchangeRateView() {
        TextField fromField = new TextField("Moeda de origem (ex: EUR)");
        fromField.setValue("EUR");

        TextField toField = new TextField("Moeda de destino (ex: USD)");
        toField.setValue("USD");

        Button button = new Button("Obter taxa de câmbio", event -> {
            ExchangeRateService service = new ExchangeRateService();
            try {
                double rate = service.getExchangeRate(fromField.getValue(), toField.getValue());
                Notification.show("1 " + fromField.getValue() + " = " + rate + " " + toField.getValue());
            } catch (Exception e) {
                Notification.show("Erro ao obter taxa de câmbio: " + e.getMessage());
            }
        });

        add(fromField, toField, button);
    }
}
