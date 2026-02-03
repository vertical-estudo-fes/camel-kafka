package com.empresa.demo.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class TicketAiService {

    private final ChatModel chatModel;

    // O Spring Boot injeta automaticamente o modelo Google GenAI configurado
    public TicketAiService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String analisarTicket(String descricaoChamado) {
        String prompt = "Atue como um especialista de suporte técnico sênior. " +
                "Resuma este chamado técnico e sugira uma solução técnica direta. " +
                "Chamado: " + descricaoChamado;

        // O método .call() envia o prompt e retorna a resposta (String)
        return chatModel.call(prompt);
    }
}