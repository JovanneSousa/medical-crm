package com.omnimed.crm.service;

import com.omnimed.crm.channel.DespachanteCanalService;
import com.omnimed.crm.model.Mensagem;
import com.omnimed.crm.model.RemetenteTipo;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.model.StatusSessao;
import com.omnimed.crm.repository.MensagemRepository;
import com.omnimed.crm.repository.SessaoAtendimentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatSessionTimeoutService {

    private static final Logger log = LoggerFactory.getLogger(ChatSessionTimeoutService.class);

    @Value("${omnimed.chat.inactivity-timeout-minutes:10}")
    private long inactivityTimeoutMinutes;

    private final SessaoAtendimentoRepository sessaoRepository;
    private final MensagemRepository mensagemRepository;
    private final DespachanteCanalService despachanteCanalService;

    public ChatSessionTimeoutService(SessaoAtendimentoRepository sessaoRepository,
                                     MensagemRepository mensagemRepository,
                                     DespachanteCanalService despachanteCanalService) {
        this.sessaoRepository = sessaoRepository;
        this.mensagemRepository = mensagemRepository;
        this.despachanteCanalService = despachanteCanalService;
    }

    @Scheduled(fixedDelayString = "${omnimed.chat.timeout-check-interval-ms:60000}")
    @Transactional
    public void verificarSessoesInativas() {
        if (inactivityTimeoutMinutes <= 0) {
            return;
        }

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(inactivityTimeoutMinutes);
        List<StatusSessao> statusMonitorados = List.of(StatusSessao.BOT, StatusSessao.AGUARDANDO_HUMANO);

        List<SessaoAtendimento> sessoesInativas = sessaoRepository.findByStatusInAndDataUltimaInteracaoBefore(
                statusMonitorados, cutoff
        );

        if (!sessoesInativas.isEmpty()) {
            log.info("⏰ Encontradas {} sessões inativas para encerramento automático (limite: {} min)",
                    sessoesInativas.size(), inactivityTimeoutMinutes);
        }

        for (SessaoAtendimento sessao : sessoesInativas) {
            sessao.finalizar();
            sessaoRepository.save(sessao);

            String aviso = "⏳ *Atendimento encerrado por inatividade.*\n\n"
                    + "Identificamos que não houve interação recente nos últimos " + inactivityTimeoutMinutes + " minutos.\n"
                    + "Esta sessão foi encerrada com segurança.\n\n"
                    + "Quando desejar falar conosco novamente, basta enviar um 'olá' ou digitar *menu*! 🩺";

            mensagemRepository.save(new Mensagem(sessao, RemetenteTipo.SISTEMA, aviso));
            despachanteCanalService.enviarMensagem(sessao.getCanal(), sessao.getCanalUsuarioId(), aviso);
        }
    }
}
