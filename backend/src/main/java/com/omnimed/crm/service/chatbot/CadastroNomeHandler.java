package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.SessaoAtendimento;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Handler responsável pela coleta e validação do nome durante o cadastro guiado.
 */
@Component
public class CadastroNomeHandler extends AbstractEstadoConversaHandler {

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.CADASTRO_NOME);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        if (texto.isBlank() || texto.trim().length() < 3) {
            return "❌ Por favor, informe seu nome e sobrenome completos (pelo menos 3 letras):";
        }

        String nomeLimpo = texto.trim();
        sessao.registrarNomeCadastro(nomeLimpo);

        String dicaTelefone = canal.obterDicaTelefoneCadastro().orElse("");
        return "Muito prazer, *" + nomeLimpo + "*! 🤝\n\n"
                + "Agora, por favor informe seu *número de telefone / celular com DDD* (Ex: 11988887777):"
                + dicaTelefone;
    }
}
