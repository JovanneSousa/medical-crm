package com.omnimed.crm.service;

import com.omnimed.crm.dto.AgendamentoDTO;
import com.omnimed.crm.model.*;
import com.omnimed.crm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ConversationalEngineService {

    private final SessaoAtendimentoRepository sessaoRepository;
    private final MensagemRepository mensagemRepository;
    private final PacienteRepository pacienteRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final MedicoRepository medicoRepository;
    private final HorarioAtendimentoRepository horarioRepository;
    private final AgendamentoService agendamentoService;
    private final ExameService exameService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Set<String> COMANDOS_ENCERRAR = Set.of("sair", "/sair", "encerrar", "/encerrar", "fim", "finalizar", "tchau");

    public ConversationalEngineService(SessaoAtendimentoRepository sessaoRepository,
                                       MensagemRepository mensagemRepository,
                                       PacienteRepository pacienteRepository,
                                       EspecialidadeRepository especialidadeRepository,
                                       MedicoRepository medicoRepository,
                                       HorarioAtendimentoRepository horarioRepository,
                                       AgendamentoService agendamentoService,
                                       ExameService exameService) {
        this.sessaoRepository = sessaoRepository;
        this.mensagemRepository = mensagemRepository;
        this.pacienteRepository = pacienteRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.medicoRepository = medicoRepository;
        this.horarioRepository = horarioRepository;
        this.agendamentoService = agendamentoService;
        this.exameService = exameService;
    }

    @Transactional
    public SessaoAtendimento obterOuCriarSessao(CanalTipo canal, String canalUsuarioId) {
        // Tenta buscar sessão ativa mais recente
        Optional<SessaoAtendimento> sessaoAtivaOpt = sessaoRepository
                .findTopByCanalAndCanalUsuarioIdAndStatusNotOrderByDataUltimaInteracaoDesc(canal, canalUsuarioId, StatusSessao.FINALIZADO);

        if (sessaoAtivaOpt.isPresent()) {
            return sessaoAtivaOpt.get();
        }

        // Se não houver sessão ativa (ou a anterior foi finalizada), cria uma nova
        SessaoAtendimento nova = new SessaoAtendimento(canal, canalUsuarioId);
        if (canal == CanalTipo.TELEGRAM) {
            pacienteRepository.findByTelegramChatId(canalUsuarioId).ifPresent(nova::setPaciente);
        } else if (canal == CanalTipo.WHATSAPP) {
            String cleanNumber = canalUsuarioId.replaceAll("[^0-9]", "");
            pacienteRepository.findByWhatsappNumber(cleanNumber)
                    .or(() -> pacienteRepository.findByWhatsappNumber(canalUsuarioId))
                    .ifPresent(nova::setPaciente);
        }

        return sessaoRepository.save(nova);
    }

    @Transactional
    public String processarMensagem(CanalTipo canal, String canalUsuarioId, String textoOriginal) {
        SessaoAtendimento sessao = obterOuCriarSessao(canal, canalUsuarioId);
        sessao.setDataUltimaInteracao(LocalDateTime.now());

        String texto = (textoOriginal != null) ? textoOriginal.trim() : "";

        // 1. Gravar mensagem do paciente
        Mensagem msgPaciente = new Mensagem(sessao, RemetenteTipo.PACIENTE, texto);
        mensagemRepository.save(msgPaciente);

        // Se a sessão estava finalizada e o usuário enviou nova mensagem, reabre como bot
        if (sessao.getStatus() == StatusSessao.FINALIZADO) {
            sessao.setStatus(StatusSessao.BOT);
            if (sessao.getPaciente() != null) {
                sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
            } else {
                sessao.setEstadoConversa(EstadoConversa.AGUARDANDO_CPF);
            }
        }

        // 2. Se a sessão estiver em atendimento humano, não responder com o bot
        if (sessao.getStatus() == StatusSessao.EM_ATENDIMENTO_HUMANO) {
            sessaoRepository.save(sessao);
            return null; // O atendente responderá pelo painel web
        }

        // 3. Safety Boundary - Triagem de emergência médica
        if (isEmergencia(texto)) {
            String alertaEmergencia = "⚠️ *ATENÇÃO - SITUAÇÃO DE EMERGÊNCIA DETECTADA*\n\n"
                    + "Esta plataforma não realiza diagnóstico médico nem substitui atendimento emergencial.\n\n"
                    + "Se você ou alguém próximo está apresentando sintomas graves (como dor no peito, falta de ar severa ou perda de consciência):\n"
                    + "🚨 Ligue imediatamente para o *SAMU (192)* ou *Bombeiros (193)*\n"
                    + "🏥 Dirija-se imediatamente ao pronto-socorro mais próximo.";
            gravarRespostaBot(sessao, alertaEmergencia);
            sessaoRepository.save(sessao);
            return alertaEmergencia;
        }

        // 4. Comando explícito de encerramento pelo cliente
        String textoLower = texto.toLowerCase();
        if (COMANDOS_ENCERRAR.contains(textoLower)) {
            return finalizarAtendimentoCliente(sessao);
        }

        // 5. Comando de reset / voltar ao menu
        if (textoLower.equals("menu") || textoLower.equals("/menu") || textoLower.equals("inicio") || textoLower.equals("/start")) {
            sessao.setTempEspecialidadeId(null);
            sessao.setTempMedicoId(null);
            sessao.setTempCpf(null);
            sessao.setTempNome(null);
            sessao.setTempTelefone(null);

            if (sessao.getPaciente() == null) {
                sessao.setEstadoConversa(EstadoConversa.AGUARDANDO_CPF);
                String msg = "👋 *Olá! Bem-vindo à Clínica OmniMed Care.*\n\n"
                        + "Sou seu assistente virtual de atendimento integrado.\n"
                        + "Para localizar seu cadastro com segurança, por favor *digite seu CPF* (apenas números ou com pontos/traço):";
                gravarRespostaBot(sessao, msg);
                sessaoRepository.save(sessao);
                return msg;
            } else {
                sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
                String msg = gerarMenuPrincipal(sessao.getPaciente().getNome());
                gravarRespostaBot(sessao, msg);
                sessaoRepository.save(sessao);
                return msg;
            }
        }

        // 6. Máquina de estados
        String respostaBot = switch (sessao.getEstadoConversa()) {
            case INICIO, AGUARDANDO_CPF -> tratarAguardandoCpf(sessao, texto, canal, canalUsuarioId);
            case CADASTRO_NOME -> tratarCadastroNome(sessao, texto, canal, canalUsuarioId);
            case CADASTRO_TELEFONE -> tratarCadastroTelefone(sessao, texto, canal, canalUsuarioId);
            case CADASTRO_EMAIL -> tratarCadastroEmail(sessao, texto, canal, canalUsuarioId);
            case MENU_PRINCIPAL -> tratarMenuPrincipal(sessao, texto);
            case ESCOLHENDO_ESPECIALIDADE -> tratarEscolhaEspecialidade(sessao, texto);
            case ESCOLHENDO_MEDICO -> tratarEscolhaMedico(sessao, texto);
            case ESCOLHENDO_HORARIO -> tratarEscolhaHorario(sessao, texto, canal);
            case CONSULTANDO_AGENDAMENTOS -> tratarConsultaAgendamentos(sessao, texto);
            case CONSULTANDO_EXAME -> tratarConsultaExames(sessao, texto);
            case ATENDIMENTO_HUMANO -> "Sua solicitação está na fila da recepção. Um atendente entrará em contato em breve.";
            case FINALIZADO -> {
                sessao.setStatus(StatusSessao.BOT);
                sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
                yield (sessao.getPaciente() != null)
                        ? gerarMenuPrincipal(sessao.getPaciente().getNome())
                        : "Por favor, digite *menu* para iniciar.";
            }
            default -> {
                sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
                yield (sessao.getPaciente() != null)
                        ? gerarMenuPrincipal(sessao.getPaciente().getNome())
                        : "Por favor, digite *menu* para iniciar.";
            }
        };

        gravarRespostaBot(sessao, respostaBot);
        sessaoRepository.save(sessao);
        return respostaBot;
    }

    private String finalizarAtendimentoCliente(SessaoAtendimento sessao) {
        sessao.setStatus(StatusSessao.FINALIZADO);
        sessao.setEstadoConversa(EstadoConversa.FINALIZADO);
        sessao.setTempEspecialidadeId(null);
        sessao.setTempMedicoId(null);
        sessao.setTempCpf(null);
        sessao.setTempNome(null);
        sessao.setTempTelefone(null);

        String msgDespedida = "👋 *Atendimento Finalizado com Sucesso!*\n\n"
                + "Muito obrigado por entrar em contato com a *OmniMed Care*. Cuidar da sua saúde é a nossa prioridade!\n\n"
                + "Caso precise de novo atendimento a qualquer momento, basta enviar uma mensagem ou digitar *menu*.\n\n"
                + "Tenha um excelente dia! 🩺✨";

        gravarRespostaBot(sessao, msgDespedida);
        sessaoRepository.save(sessao);
        return msgDespedida;
    }

    private String tratarAguardandoCpf(SessaoAtendimento sessao, String texto, CanalTipo canal, String canalUsuarioId) {
        String cpfLimpo = texto.replaceAll("[^0-9]", "");
        if (cpfLimpo.length() != 11) {
            return "❌ O CPF informado parece inválido. Por favor, digite os *11 dígitos do seu CPF* para prosseguir:";
        }

        Optional<Paciente> pacienteOpt = pacienteRepository.findByCpf(cpfLimpo);
        if (pacienteOpt.isPresent()) {
            Paciente paciente = pacienteOpt.get();
            if (canal == CanalTipo.TELEGRAM && (paciente.getTelegramChatId() == null || paciente.getTelegramChatId().isBlank())) {
                paciente.setTelegramChatId(canalUsuarioId);
                pacienteRepository.save(paciente);
            } else if (canal == CanalTipo.WHATSAPP && (paciente.getWhatsappNumber() == null || paciente.getWhatsappNumber().isBlank())) {
                paciente.setWhatsappNumber(canalUsuarioId.replaceAll("[^0-9]", ""));
                pacienteRepository.save(paciente);
            }

            sessao.setPaciente(paciente);
            sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
            return "✅ Identificação realizada com sucesso!\n\n" + gerarMenuPrincipal(paciente.getNome());
        } else {
            // Inicia fluxo conversacional interativo de cadastro
            sessao.setTempCpf(cpfLimpo);
            sessao.setEstadoConversa(EstadoConversa.CADASTRO_NOME);

            String cpfFormatado = formatarCpf(cpfLimpo);
            return "🔍 *Não localizamos um cadastro para o CPF informado (" + cpfFormatado + ").*\n\n"
                    + "Para agendar consultas e consultar seus exames, vamos realizar seu cadastro agora mesmo. É super rápido!\n\n"
                    + "👉 *Por favor, digite o seu Nome Completo:*";
        }
    }

    private String tratarCadastroNome(SessaoAtendimento sessao, String texto, CanalTipo canal, String canalUsuarioId) {
        if (texto.isBlank() || texto.trim().length() < 3) {
            return "❌ Por favor, informe seu nome e sobrenome completos (pelo menos 3 letras):";
        }

        sessao.setTempNome(texto.trim());
        sessao.setEstadoConversa(EstadoConversa.CADASTRO_TELEFONE);

        String dicaTelefone = "";
        if (canal == CanalTipo.WHATSAPP) {
            dicaTelefone = "\n_(Ou digite *sim* para confirmar o uso do número deste WhatsApp)_";
        }

        return "Muito prazer, *" + texto.trim() + "*! 🤝\n\n"
                + "Agora, por favor informe seu *número de telefone / celular com DDD* (Ex: 11988887777):"
                + dicaTelefone;
    }

    private String tratarCadastroTelefone(SessaoAtendimento sessao, String texto, CanalTipo canal, String canalUsuarioId) {
        String telefone;
        if (canal == CanalTipo.WHATSAPP && (texto.equalsIgnoreCase("sim") || texto.equalsIgnoreCase("ok") || texto.equalsIgnoreCase("usar este"))) {
            telefone = canalUsuarioId.replaceAll("[^0-9]", "");
        } else {
            telefone = texto.replaceAll("[^0-9]", "");
        }

        if (telefone.length() < 8) {
            return "❌ Número de telefone inválido. Por favor, digite seu número com DDD (ex: 11988887777):";
        }

        sessao.setTempTelefone(telefone);
        sessao.setEstadoConversa(EstadoConversa.CADASTRO_EMAIL);

        return "Perfeito! 📧\n\n"
                + "Por fim, informe seu *e-mail* para envio de confirmações e laudos\n"
                + "_(ou digite *pular* caso não deseje cadastrar e-mail agora)_:";
    }

    private String tratarCadastroEmail(SessaoAtendimento sessao, String texto, CanalTipo canal, String canalUsuarioId) {
        String email = texto.equalsIgnoreCase("pular") ? "" : texto.trim();

        // Salva novo paciente
        Paciente novoPaciente = new Paciente(
                sessao.getTempNome(),
                sessao.getTempCpf(),
                sessao.getTempTelefone(),
                email,
                canal == CanalTipo.TELEGRAM ? canalUsuarioId : null,
                canal == CanalTipo.WHATSAPP ? canalUsuarioId.replaceAll("[^0-9]", "") : null
        );

        novoPaciente = pacienteRepository.save(novoPaciente);

        sessao.setPaciente(novoPaciente);
        sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
        sessao.setTempCpf(null);
        sessao.setTempNome(null);
        sessao.setTempTelefone(null);

        return "🎉 *CADASTRO REALIZADO COM SUCESSO!*\n\n"
                + "Seja muito bem-vindo(a) à OmniMed Care, *" + novoPaciente.getNome() + "*!\n\n"
                + gerarMenuPrincipal(novoPaciente.getNome());
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

    private String tratarMenuPrincipal(SessaoAtendimento sessao, String texto) {
        switch (texto) {
            case "1" -> {
                sessao.setEstadoConversa(EstadoConversa.ESCOLHENDO_ESPECIALIDADE);
                List<Especialidade> especialidades = especialidadeRepository.findAll();
                StringBuilder sb = new StringBuilder("🩺 *Selecione a Especialidade Desejada:*\n\n");
                for (int i = 0; i < especialidades.size(); i++) {
                    sb.append(i + 1).append("️⃣ ").append(especialidades.get(i).getNome()).append("\n");
                }
                sb.append("\n👉 _Digite o número correspondente (ou 'menu' para voltar):_");
                return sb.toString();
            }
            case "2" -> {
                sessao.setEstadoConversa(EstadoConversa.CONSULTANDO_AGENDAMENTOS);
                List<AgendamentoDTO> agendamentos = agendamentoService.listarPorPaciente(sessao.getPaciente().getId());
                if (agendamentos.isEmpty()) {
                    sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
                    return "ℹ️ Você não possui agendamentos ativos no momento.\n\nDigite *1* para marcar uma consulta ou *menu* para voltar.";
                }
                StringBuilder sb = new StringBuilder("📅 *Seus Agendamentos:*\n\n");
                for (int i = 0; i < agendamentos.size(); i++) {
                    AgendamentoDTO a = agendamentos.get(i);
                    sb.append(i + 1).append(". *").append(a.especialidadeNome()).append("*\n")
                      .append("   Médico: ").append(a.medicoNome()).append("\n")
                      .append("   Data: ").append(a.dataHora().format(DATE_FORMATTER)).append("\n")
                      .append("   Status: ").append(a.status()).append("\n")
                      .append("   Protocolo: `").append(a.protocolo()).append("`\n\n");
                }
                sb.append("Para cancelar algum agendamento, digite *cancelar <número do item>*\nOu digite *menu* para voltar ao menu principal.");
                return sb.toString();
            }
            case "3" -> {
                sessao.setEstadoConversa(EstadoConversa.CONSULTANDO_EXAME);
                List<Exame> exames = exameService.listarPorPaciente(sessao.getPaciente().getId());
                if (exames.isEmpty()) {
                    sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
                    return "🔬 Não foram encontrados exames recentes vinculados ao seu cadastro.\n\nDigite *menu* para retornar ao início.";
                }
                StringBuilder sb = new StringBuilder("🔬 *Seus Exames e Laudos:*\n\n");
                for (Exame e : exames) {
                    sb.append("• *").append(e.getTipoExame()).append("*\n")
                      .append("  Código: ").append(e.getCodigoExame()).append("\n")
                      .append("  Data da Coleta: ").append(e.getDataColeta()).append("\n")
                      .append("  Status: *").append(e.getStatus()).append("*\n");
                    if (e.getLaudoUrl() != null && !e.getLaudoUrl().isBlank()) {
                        sb.append("  📄 Laudo online: ").append(e.getLaudoUrl()).append("\n");
                    }
                    sb.append("\n");
                }
                sb.append("👉 _Digite 'menu' para voltar ao menu principal ou 'encerrar' para finalizar._");
                return sb.toString();
            }
            case "4" -> {
                return "🏥 *OmniMed Care - Informações Gerais*\n\n"
                        + "📍 *Endereço:* Av. Paulista, 1500 - 8º Andar, São Paulo - SP\n"
                        + "🕒 *Horário de Atendimento:* Seg a Sex das 07h às 19h | Sáb das 08h às 13h\n"
                        + "💳 *Convênios Aceitos:* Unimed, Bradesco Saúde, SulAmérica, Amil, NotreDame Intermédica e Particular.\n"
                        + "📞 *Telefone Central:* (11) 4002-8922\n\n"
                        + "👉 _Digite 'menu' para voltar ao início ou 'encerrar' para finalizar._";
            }
            case "5" -> {
                sessao.setStatus(StatusSessao.AGUARDANDO_HUMANO);
                sessao.setEstadoConversa(EstadoConversa.ATENDIMENTO_HUMANO);
                return "👤 *Transferindo para Atendimento Humano...*\n\n"
                        + "Você foi colocado(a) na fila de prioridade da nossa recepção.\n"
                        + "Um atendente humano assumirá esta conversa em breve aqui mesmo neste canal.\n\n"
                        + "_Por favor, descreva detalhadamente sua solicitação enquanto aguarda._";
            }
            case "6" -> {
                return finalizarAtendimentoCliente(sessao);
            }
            default -> {
                return "Opção não reconhecida. Por favor, digite um número de *1 a 6*, ou digite *menu*.";
            }
        }
    }

    private String tratarEscolhaEspecialidade(SessaoAtendimento sessao, String texto) {
        List<Especialidade> especialidades = especialidadeRepository.findAll();
        try {
            int escolha = Integer.parseInt(texto) - 1;
            if (escolha >= 0 && escolha < especialidades.size()) {
                Especialidade esp = especialidades.get(escolha);
                sessao.setTempEspecialidadeId(esp.getId());
                sessao.setEstadoConversa(EstadoConversa.ESCOLHENDO_MEDICO);

                List<Medico> medicos = medicoRepository.findByEspecialidadeIdAndAtivoTrue(esp.getId());
                if (medicos.isEmpty()) {
                    sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
                    return "ℹ️ Nenhum médico ativo encontrado para a especialidade " + esp.getNome() + ".\nDigite *menu* para voltar.";
                }

                StringBuilder sb = new StringBuilder("👨‍⚕️ *Médicos Disponíveis em " + esp.getNome() + ":*\n\n");
                for (int i = 0; i < medicos.size(); i++) {
                    sb.append(i + 1).append("️⃣ ").append(medicos.get(i).getNome())
                      .append(" (CRM: ").append(medicos.get(i).getCrm()).append(")\n");
                }
                sb.append("\n👉 _Digite o número do médico desejado:_");
                return sb.toString();
            }
        } catch (NumberFormatException ignored) {}

        return "Por favor, digite o número válido da especialidade listada (ou *menu* para voltar).";
    }

    private String tratarEscolhaMedico(SessaoAtendimento sessao, String texto) {
        List<Medico> medicos = medicoRepository.findByEspecialidadeIdAndAtivoTrue(sessao.getTempEspecialidadeId());
        try {
            int escolha = Integer.parseInt(texto) - 1;
            if (escolha >= 0 && escolha < medicos.size()) {
                Medico medico = medicos.get(escolha);
                sessao.setTempMedicoId(medico.getId());
                sessao.setEstadoConversa(EstadoConversa.ESCOLHENDO_HORARIO);

                List<HorarioAtendimento> horarios = agendamentoService.listarHorariosDisponiveis(medico.getId());
                if (horarios.isEmpty()) {
                    sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
                    return "ℹ️ O(A) Dr(a). " + medico.getNome() + " não possui horários livres nos próximos dias.\nDigite *menu* para tentar outro médico.";
                }

                StringBuilder sb = new StringBuilder("⏰ *Horários Livres com Dr(a). " + medico.getNome() + ":*\n\n");
                int limit = Math.min(horarios.size(), 8);
                for (int i = 0; i < limit; i++) {
                    HorarioAtendimento h = horarios.get(i);
                    sb.append(i + 1).append("️⃣ ")
                      .append(h.getData().format(DateTimeFormatter.ofPattern("dd/MM (EEEE)")))
                      .append(" às ").append(h.getHoraInicio()).append("\n");
                }
                sb.append("\n👉 _Digite o número do horário que prefere:_");
                return sb.toString();
            }
        } catch (NumberFormatException ignored) {}

        return "Por favor, digite o número do médico desejado (ou *menu* para voltar).";
    }

    private String tratarEscolhaHorario(SessaoAtendimento sessao, String texto, CanalTipo canal) {
        List<HorarioAtendimento> horarios = agendamentoService.listarHorariosDisponiveis(sessao.getTempMedicoId());
        try {
            int escolha = Integer.parseInt(texto) - 1;
            if (escolha >= 0 && escolha < horarios.size()) {
                HorarioAtendimento horarioEscolhido = horarios.get(escolha);
                Medico medico = medicoRepository.findById(sessao.getTempMedicoId()).orElseThrow();
                Paciente paciente = sessao.getPaciente();

                Agendamento agendamento = agendamentoService.criarAgendamento(
                        paciente, medico, horarioEscolhido, canal
                );

                sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
                sessao.setTempEspecialidadeId(null);
                sessao.setTempMedicoId(null);

                return "🎉 *CONSULTA AGENDADA COM SUCESSO!*\n\n"
                        + "📌 *Protocolo:* `" + agendamento.getProtocolo() + "`\n"
                        + "👤 *Paciente:* " + paciente.getNome() + "\n"
                        + "🩺 *Especialidade:* " + medico.getEspecialidade().getNome() + "\n"
                        + "👨‍⚕️ *Médico:* " + medico.getNome() + "\n"
                        + "🗓️ *Data e Hora:* " + agendamento.getDataHora().format(DATE_FORMATTER) + "\n"
                        + "📍 *Local:* Av. Paulista, 1500 - 8º Andar\n\n"
                        + "Enviaremos lembrete antes do horário. Obrigado por escolher a OmniMed Care!\n\n"
                        + "👉 _Digite 'menu' para voltar ao início ou 'encerrar' para finalizar o atendimento._";
            }
        } catch (NumberFormatException ignored) {}

        return "Opção de horário inválida. Por favor, selecione um número da lista ou digite *menu*.";
    }

    private String tratarConsultaAgendamentos(SessaoAtendimento sessao, String texto) {
        if (texto.toLowerCase().startsWith("cancelar")) {
            String[] parts = texto.split(" ");
            if (parts.length >= 2) {
                try {
                    int index = Integer.parseInt(parts[1]) - 1;
                    List<AgendamentoDTO> lista = agendamentoService.listarPorPaciente(sessao.getPaciente().getId());
                    if (index >= 0 && index < lista.size()) {
                        agendamentoService.cancelarAgendamento(lista.get(index).id());
                        sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
                        return "✅ Agendamento cancelado com sucesso. O horário foi liberado em nossa agenda.\n\nDigite *menu* para voltar ou *encerrar* para finalizar.";
                    }
                } catch (Exception ignored) {}
            }
            return "Formato incorreto. Use: *cancelar <número>* (ex: cancelar 1) ou *menu* para voltar.";
        }
        sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
        return gerarMenuPrincipal(sessao.getPaciente().getNome());
    }

    private String tratarConsultaExames(SessaoAtendimento sessao, String texto) {
        sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
        return gerarMenuPrincipal(sessao.getPaciente().getNome());
    }

    private boolean isEmergencia(String texto) {
        String lower = texto.toLowerCase();
        return lower.contains("infarto") || lower.contains("parada cardíaca") || lower.contains("parada cardiaca")
                || lower.contains("falta de ar grave") || lower.contains("sangramento grave")
                || lower.contains("perda de consciência") || lower.contains("socorro") || lower.contains("emergência");
    }

    private void gravarRespostaBot(SessaoAtendimento sessao, String conteudo) {
        if (conteudo != null && !conteudo.isBlank()) {
            Mensagem msg = new Mensagem(sessao, RemetenteTipo.BOT, conteudo);
            mensagemRepository.save(msg);
        }
    }

    private String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }
}
