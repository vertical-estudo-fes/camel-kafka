package com.empresa.demo.routes;

import com.empresa.demo.dto.TicketDTO;
import com.empresa.demo.model.TicketEntity;
import com.empresa.demo.service.TicketAiService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TicketRoute extends RouteBuilder {

    @Autowired
    private TicketAiService ticketAiService;

    @Override
    public void configure() throws Exception {

        // --- ROTA 1: Producer (API -> Kafka) ---
        from("direct:receberTicket")
                .routeId("Ticket-Step1-PublishKafka")
                .log("Recebendo ticket via API: ${body.description}")
                .marshal().json(JsonLibrary.Jackson)
                // Nota: Usamos apenas o nome do tópico, pois o broker está no application.properties
                .to("kafka:ticket.incoming")
                .log("Ticket enviado para fila Kafka: ticket.incoming");


        // --- ROTA 2: Consumer (Kafka -> AI -> MySQL) ---
        from("kafka:ticket.incoming")
                .routeId("Ticket-Step2-EnrichAndSave")
                .log("Consumindo ticket do Kafka...")

                // Converte JSON do Kafka de volta para DTO Java
                .unmarshal().json(JsonLibrary.Jackson, TicketDTO.class)

                .process(exchange -> {
                    // 1. Pega os dados originais
                    TicketDTO dto = exchange.getIn().getBody(TicketDTO.class);

                    // 2. Chama o Serviço de IA (LangChain4j + Gemini)
                    String sugestaoIA = ticketAiService.analisarTicket(dto.getDescription());

                    // 3. Monta a entidade para salvar no banco
                    TicketEntity entity = new TicketEntity();
                    entity.setDescription(dto.getDescription());
                    entity.setSuggestion(sugestaoIA); // Campo suggestion_ia

                    // Define a entidade como novo corpo da mensagem
                    exchange.getIn().setBody(entity);
                })

                .log("IA processou. Salvando no banco: ${body}")
                .to("jpa:com.empresa.demo.model.TicketEntity");
    }
}