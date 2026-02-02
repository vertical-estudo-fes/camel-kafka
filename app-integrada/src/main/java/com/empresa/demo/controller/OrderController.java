package com.empresa.demo.controller;

import com.empresa.demo.model.OrderEntity;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private ProducerTemplate producer;

    @PostMapping
    public String createOrder(@RequestBody OrderEntity order) {
        order.setStatus("PENDING");
        // Inicia a SAGA enviando para a rota do Camel
        producer.sendBody("direct:startSaga", order);
        return "Pedido recebido! Processamento assíncrono iniciado.";
    }
}