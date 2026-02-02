package com.empresa.demo.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CqrsRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        // O Debezium publica no tópico: dbserver1.orderdb.orders
        from("kafka:dbserver1.orderdb.orders?brokers=kafka:29092")
                .routeId("CQRS-RealTime-Sync")
                .log("📡 CDC Evento recebido do MySQL!")

                // O corpo vem como JSON do Debezium. Estrutura: { "payload": { "after": { ...dados... } } }
                .unmarshal().json(JsonLibrary.Jackson, Map.class)

                // Filtra apenas eventos de Create/Update (ignora delete ou nulls)
                .filter(simple("${body[payload][after]} != null"))

                .process(exchange -> {
                    Map fullJson = exchange.getIn().getBody(Map.class);
                    Map payload = (Map) fullJson.get("payload");
                    Map after = (Map) payload.get("after");

                    // TRANSFORMACAO (ETL): Calcular algo novo para o relatório
                    after.put("status", "PROCESSADO_PELO_CAMEL");
                    after.put("data_sincronizacao", System.currentTimeMillis());
                    after.put("origem", "CDC-Pipeline");

                    exchange.getIn().setBody(after);
                })

                .log("💾 Salvando no MongoDB (Leitura Otimizada): ${body}")
                .to("mongodb:mongoBean?database=analyticsdb&collection=orders_report&operation=save");
    }
}

// Configuração necessária para o Bean do Mongo
@Configuration
class MongoConfig {
    @Bean
    public com.mongodb.client.MongoClient mongoBean() {
        return com.mongodb.client.MongoClients.create("mongodb://mongodb:27017");
    }
}