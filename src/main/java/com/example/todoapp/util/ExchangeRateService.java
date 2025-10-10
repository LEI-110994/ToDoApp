package com.example.todoapp.util;

import org.apache.http.client.fluent.Request;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExchangeRateService {

    public double getExchangeRate(String from, String to) throws Exception {
        // Endpoint da API pública (não é preciso chave)
        String url = String.format("https://api.frankfurter.app/latest?from=%s&to=%s", from, to);

        // Pedido HTTP à API
        String response = Request.Get(url)
                .connectTimeout(200000)
                .socketTimeout(200000)
                .execute().returnContent().asString();

        // Parsing do JSON de resposta
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);

        // Extrai a taxa de câmbio da resposta
        return node.get("rates").get(to).asDouble();
    }
    public static void main(String[] args) {
        ExchangeRateService service = new ExchangeRateService();
        try {
            double rate = service.getExchangeRate("EUR", "USD");
            System.out.println("Câmbio EUR para USD: " + rate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
