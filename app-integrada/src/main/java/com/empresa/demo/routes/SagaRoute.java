package com.empresa.demo.routes;

import com.empresa.demo.model.OrderEntity;
import com.empresa.demo.service.OrderService;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SagaRoute extends RouteBuilder {
    
    private OrderService orderService;

    @Override
    public void configure() throws Exception {

        // 1. Início: Salva no MySQL e Notifica Kafka
        from("direct:startSaga")
                .routeId("Saga-Step1-CreateOrder")
                .log("1. Recebido pedido: ${body.item}")
                .to("jpa:com.empresa.demo.model.OrderEntity") // Salva PENDING no MySQL
                .marshal().json()
                .to("kafka:pedido.criado?brokers=kafka:29092")
                .log("2. Evento 'pedido.criado' enviado ao Kafka");

        // 2. Simulação de Pagamento (Consome Kafka)
        from("kafka:pedido.criado?brokers=kafka:29092")
                .routeId("Saga-Step2-ProcessPayment")
                .unmarshal().json(JsonLibrary.Jackson, Map.class)
                .log("3. Processando pagamento para pedido ID: ${body[id]}")

                // LÓGICA DE SIMULAÇÃO DE ERRO
                .choice()
                .when(simple("${body[price]} > 1000")) // Se for caro, falha!
                .log("❌ ERRO: Saldo insuficiente!")
                .marshal().json(JsonLibrary.Jackson) 
                .to("kafka:pagamento.falhou?brokers=kafka:29092")
                .otherwise()
                .log("✅ Pagamento Aprovado!")
                .marshal().json(JsonLibrary.Jackson) 
                .to("kafka:pagamento.sucesso?brokers=kafka:29092")
                .end();

        // 3. Compensação (Rollback)
        from("kafka:pagamento.falhou?brokers=kafka:29092")
                .routeId("Saga-Step3-Compensation")
                .unmarshal().json(JsonLibrary.Jackson, Map.class)
                .log("⚠️ Iniciando COMPENSAÇÃO para pedido ID: ${body[id]}")

                // Atualiza status para CANCELLED no MySQL
                .process(exchange -> {
                    Map data = exchange.getIn().getBody(Map.class);
                    OrderEntity order = orderService.buscaPedidoPorId(Long.valueOf(data.get("id").toString())); 
                    order.setStatus("CANCELLED"); // Atualização
                    exchange.getIn().setBody(order);
                })
                // Aqui usamos um truque: JPA update funciona se o ID existir
                .to("jpa:com.empresa.demo.model.OrderEntity?usePersist=false")
                .log("🔄 Pedido estornado (Rollback) com sucesso!");
    }
}
