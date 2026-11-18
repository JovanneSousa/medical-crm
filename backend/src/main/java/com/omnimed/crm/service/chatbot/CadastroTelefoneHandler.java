package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.SessaoAtendimento;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Handler responsável pela coleta do telefone durante o cadastro,
 * utilizando o comportamento polimórfico do canal para atalhos (ex.: WhatsApp).
 */
@Component
public class CadastroTelefoneHandler extends AbstractEstadoConversaHandler {

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.CADASTRO_TELEFONE);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        String telefone = canal.resolverTelefoneCadastro(texto, sessao.getCanalUsuarioId())
                .orElseGet(() -> texto.replaceAll("[^0-9]", ""));

        if (telefone.length() < 8) {
            return "❌ Número de telefone inválido. Por favor, digite seu número com DDD (ex: 11988887777):";
        }

        sessao.registrarTelefoneCadastro(telefone);
        return "Perfeito! 📧\n\n"
                + "Por fim, informe seu *e-mail* para envio de confirmações e laudos\n"
                + "_(ou digite *pular* caso não deseje cadastrar e-mail agora)_:";
    }
}
