package com.empresa.demo;

import com.empresa.demo.service.TicketAiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest // Carrega o contexto do Spring (lê application.properties e cria os Beans)
public class TicketServiceTest {

    @Autowired
    private TicketAiService ticketService;

    @Test
    void testeDeConexaoRapida() {
        System.out.println("--- Iniciando Teste de Conexão com Gemini (Spring AI) ---");

        try {
            // Chama o método do serviço que vai no Gemini
            String resposta = ticketService.analisarTicket("O aparelho de ar condicionado (Split de 12.000 BTUs) está levando muito tempo para gelar um quarto em que se encaixa no tamanho adequado para o aparelho");

            System.out.println("SUCESSO! Resposta da IA: " + resposta);

            // Validações simples
            assertNotNull(resposta);
            assertTrue(resposta.toUpperCase().contains("OK"), "A resposta deveria conter 'OK'");

        } catch (Exception e) {
            System.err.println("ERRO DE CONEXÃO: " + e.getMessage());
            throw e; // Falha o teste se der erro
        }
    }
}