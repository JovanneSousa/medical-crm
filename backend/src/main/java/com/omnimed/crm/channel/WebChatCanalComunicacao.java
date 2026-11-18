package com.omnimed.crm.channel;

import com.omnimed.crm.model.CanalTipo;
import org.springframework.stereotype.Component;

/**
 * Implementação concreta para sessões de WebChat / Simulador web.
 * Permite tratar todos os canais de forma polimórfica sem condicionais específicas.
 */
@Component
public class WebChatCanalComunicacao extends AbstractCanalComunicacao {

    @Override
    public CanalTipo getTipoCanal() {
        return CanalTipo.WEB_CHAT;
    }

    @Override
    public boolean isAtivo() {
        return true;
    }

    @Override
    protected boolean executarEnvio(String destinatarioNormalizado, String texto) {
        log.debug("🌐 [WEB_CHAT] Mensagem registrada para sessão {}", destinatarioNormalizado);
        return true;
    }
}
