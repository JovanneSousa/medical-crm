package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.SessaoAtendimento;

import java.util.Set;

/**
 * Abstração (Padrão State/Strategy) para o processamento polimórfico
 * de cada etapa da máquina de estados conversacional.
 */
public interface EstadoConversaHandler {

    Set<EstadoConversa> getEstadosSuportados();

    String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal);
}
