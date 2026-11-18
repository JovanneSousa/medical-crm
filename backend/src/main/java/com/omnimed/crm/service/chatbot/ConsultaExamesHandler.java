package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.SessaoAtendimento;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Handler responsável pelo retorno ao menu após consulta de exames.
 */
@Component
public class ConsultaExamesHandler extends AbstractEstadoConversaHandler {

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.CONSULTANDO_EXAME);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        sessao.retornarAoMenuPrincipal();
        return gerarMenuPrincipal(sessao.getPaciente().getNome());
    }
}
