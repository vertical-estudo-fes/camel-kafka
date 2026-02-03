package com.empresa.demo.controller;

import com.empresa.demo.dto.TicketDTO;
import com.empresa.demo.model.TicketEntity;
import com.empresa.demo.repository.TicketRepository;
import jakarta.websocket.server.PathParam;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private TicketRepository repository;

    @PostMapping
    public String criarTicket(@RequestBody TicketDTO ticket) {
        // Envia para a rota Direct do Camel (que enviará para o Kafka)
        producerTemplate.sendBody("direct:receberTicket", ticket);

        return "Ticket recebido! A IA está analisando sua solicitação.";
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> buscarSugestaoIa(@PathVariable("id") Long id) {
        return repository.findById(id)
                .map(view -> ResponseEntity.ok(view.getSuggestion()))
                .orElse(ResponseEntity.notFound().build());
    }
}