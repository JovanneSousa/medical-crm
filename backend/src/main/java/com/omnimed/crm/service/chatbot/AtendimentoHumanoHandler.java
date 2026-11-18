package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.SessaoAtendimento;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Handler para mensagens recebidas enquanto o paciente aguarda atendimento humano na fila.
 */
@Component
public class AtendimentoHumanoHandler extends AbstractEstadoConversaHandler {

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.ATENDIMENTO_HUMANO);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        return "Sua solicitação está na fila da recepção. Um atendente entrará em contato em breve.";
    }
}
