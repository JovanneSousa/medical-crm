package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.SessaoAtendimento;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Handler responsável por sessões finalizadas ou estados de transição residual.
 */
@Component
public class SessaoFinalizadaHandler extends AbstractEstadoConversaHandler {

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.FINALIZADO, EstadoConversa.CONFIRMANDO_AGENDAMENTO);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        sessao.reiniciarFluxoParaInicio();
        return (sessao.getPaciente() != null)
                ? gerarMenuPrincipal(sessao.getPaciente().getNome())
                : "Por favor, digite *menu* para iniciar.";
    }
}
