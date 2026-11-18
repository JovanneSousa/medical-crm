package com.omnimed.crm.service;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.channel.DespachanteCanalService;
import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.Mensagem;
import com.omnimed.crm.model.RemetenteTipo;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.model.StatusSessao;
import com.omnimed.crm.repository.MensagemRepository;
import com.omnimed.crm.repository.PacienteRepository;
import com.omnimed.crm.repository.SessaoAtendimentoRepository;
import com.omnimed.crm.service.chatbot.EstadoConversaHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Motor conversacional omnichannel da OmniMed.
 * Coordena o ciclo de vida da sessão e delega polimorficamente:
 * - aos handlers de estado ({@link EstadoConversaHandler}) o processamento de cada etapa;
 * - ao {@link DespachanteCanalService} as particularidades de cada canal ({@link CanalComunicacao}).
 */
@Service
public class ConversationalEngineService {

    private static final Set<String> COMANDOS_ENCERRAR = Set.of("sair", "/sair", "encerrar", "/encerrar", "fim", "finalizar", "tchau");
    private static final Set<String> COMANDOS_MENU = Set.of("menu", "/menu", "inicio", "/start");

    private final SessaoAtendimentoRepository sessaoRepository;
    private final MensagemRepository mensagemRepository;
    private final PacienteRepository pacienteRepository;
    private final DespachanteCanalService despachanteCanalService;
    private final Map<EstadoConversa, EstadoConversaHandler> handlersPorEstado;

    public ConversationalEngineService(SessaoAtendimentoRepository sessaoRepository,
                                       MensagemRepository mensagemRepository,
                                       PacienteRepository pacienteRepository,
                                       DespachanteCanalService despachanteCanalService,
                                       List<EstadoConversaHandler> handlers) {
        this.sessaoRepository = sessaoRepository;
        this.mensagemRepository = mensagemRepository;
        this.pacienteRepository = pacienteRepository;
        this.despachanteCanalService = despachanteCanalService;
        this.handlersPorEstado = new EnumMap<>(EstadoConversa.class);
        for (EstadoConversaHandler handler : handlers) {
            for (EstadoConversa estado : handler.getEstadosSuportados()) {
                this.handlersPorEstado.put(estado, handler);
            }
        }
    }

    @Transactional
    public SessaoAtendimento obterOuCriarSessao(CanalTipo canalTipo, String canalUsuarioId) {
        Optional<SessaoAtendimento> sessaoAtivaOpt = sessaoRepository
                .findTopByCanalAndCanalUsuarioIdAndStatusNotOrderByDataUltimaInteracaoDesc(
                        canalTipo, canalUsuarioId, StatusSessao.FINALIZADO);

        if (sessaoAtivaOpt.isPresent()) {
            return sessaoAtivaOpt.get();
        }

        CanalComunicacao canal = despachanteCanalService.obterCanal(canalTipo);
        SessaoAtendimento nova = new SessaoAtendimento(canalTipo, canalUsuarioId);
        canal.localizarPaciente(canalUsuarioId, pacienteRepository).ifPresent(nova::setPaciente);

        return sessaoRepository.save(nova);
    }

    @Transactional
    public String processarMensagem(CanalTipo canalTipo, String canalUsuarioId, String textoOriginal) {
        CanalComunicacao canal = despachanteCanalService.obterCanal(canalTipo);
        SessaoAtendimento sessao = obterOuCriarSessao(canalTipo, canalUsuarioId);
        sessao.registrarInteracao();

        String texto = (textoOriginal != null) ? textoOriginal.trim() : "";

        // 1. Gravar mensagem do paciente
        mensagemRepository.save(new Mensagem(sessao, RemetenteTipo.PACIENTE, texto));

        // 2. Reabre sessão se anteriormente finalizada
        sessao.reabrirSeFinalizada();

        // 3. Se estiver em atendimento humano, não aciona o bot
        if (sessao.isEmAtendimentoHumano()) {
            sessaoRepository.save(sessao);
            return null;
        }

        // 4. Safety Boundary - Triagem de emergência médica
        if (isEmergencia(texto)) {
            String alertaEmergencia = gerarAlertaEmergencia();
            return finalizarRespostaBot(sessao, alertaEmergencia);
        }

        // 5. Comandos globais (encerrar / voltar ao menu)
        String textoLower = texto.toLowerCase();
        if (COMANDOS_ENCERRAR.contains(textoLower)) {
            sessao.finalizar();
            return finalizarRespostaBot(sessao, gerarMensagemDespedida());
        }

        if (COMANDOS_MENU.contains(textoLower)) {
            sessao.reiniciarFluxoParaInicio();
            String msgMenu = (sessao.getPaciente() == null)
                    ? gerarSolicitacaoCpfInicial()
                    : gerarMenuPrincipal(sessao.getPaciente().getNome());
            return finalizarRespostaBot(sessao, msgMenu);
        }

        // 6. Despacho polimórfico para o handler correspondente ao estado atual
        EstadoConversaHandler handler = handlersPorEstado.get(sessao.getEstadoConversa());
        String respostaBot;
        if (handler != null) {
            respostaBot = handler.processar(sessao, texto, canal);
        } else {
            sessao.reiniciarFluxoParaInicio();
            respostaBot = (sessao.getPaciente() != null)
                    ? gerarMenuPrincipal(sessao.getPaciente().getNome())
                    : "Por favor, digite *menu* para iniciar.";
        }

        return finalizarRespostaBot(sessao, respostaBot);
    }

    private String finalizarRespostaBot(SessaoAtendimento sessao, String resposta) {
        if (resposta != null && !resposta.isBlank()) {
            mensagemRepository.save(new Mensagem(sessao, RemetenteTipo.BOT, resposta));
        }
        sessaoRepository.save(sessao);
        return resposta;
    }

    private String gerarAlertaEmergencia() {
        return "⚠️ *ATENÇÃO - SITUAÇÃO DE EMERGÊNCIA DETECTADA*\n\n"
                + "Esta plataforma não realiza diagnóstico médico nem substitui atendimento emergencial.\n\n"
                + "Se você ou alguém próximo está apresentando sintomas graves (como dor no peito, falta de ar severa ou perda de consciência):\n"
                + "🚨 Ligue imediatamente para o *SAMU (192)* ou *Bombeiros (193)*\n"
                + "🏥 Dirija-se imediatamente ao pronto-socorro mais próximo.";
    }

    private String gerarSolicitacaoCpfInicial() {
        return "👋 *Olá! Bem-vindo à Clínica OmniMed Care.*\n\n"
                + "Sou seu assistente virtual de atendimento integrado.\n"
                + "Para localizar seu cadastro com segurança, por favor *digite seu CPF* (apenas números ou com pontos/traço):";
    }

    private String gerarMenuPrincipal(String nomePaciente) {
        return "Olá, *" + nomePaciente + "*! Como podemos ajudar hoje?\n\n"
                + "1️⃣ *Agendar Consulta Médica*\n"
                + "2️⃣ *Minhas Consultas Agendadas*\n"
                + "3️⃣ *Consultar Meus Exames / Laudos*\n"
                + "4️⃣ *Informações e Convênios*\n"
                + "5️⃣ *Falar com um Atendente Humano*\n"
                + "6️⃣ *Encerrar Atendimento*\n\n"
                + "👉 _Digite o número da opção desejada:_";
    }

    private String gerarMensagemDespedida() {
        return "👋 *Atendimento Finalizado com Sucesso!*\n\n"
                + "Muito obrigado por entrar em contato com a *OmniMed Care*. Cuidar da sua saúde é a nossa prioridade!\n\n"
                + "Caso precise de novo atendimento a qualquer momento, basta enviar uma mensagem ou digitar *menu*.\n\n"
                + "Tenha um excelente dia! 🩺✨";
    }

    private boolean isEmergencia(String texto) {
        String lower = texto.toLowerCase();
        return lower.contains("infarto") || lower.contains("parada cardíaca") || lower.contains("parada cardiaca")
                || lower.contains("falta de ar grave") || lower.contains("sangramento grave")
                || lower.contains("perda de consciência") || lower.contains("socorro") || lower.contains("emergência");
    }
}
