package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.Agendamento;
import com.omnimed.crm.model.Especialidade;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.Exame;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.repository.EspecialidadeRepository;
import com.omnimed.crm.service.AgendamentoService;
import com.omnimed.crm.service.ExameService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Handler do menu principal do assistente virtual OmniMed.
 */
@Component
public class MenuPrincipalHandler extends AbstractEstadoConversaHandler {

    private final EspecialidadeRepository especialidadeRepository;
    private final AgendamentoService agendamentoService;
    private final ExameService exameService;

    public MenuPrincipalHandler(EspecialidadeRepository especialidadeRepository,
                                AgendamentoService agendamentoService,
                                ExameService exameService) {
        this.especialidadeRepository = especialidadeRepository;
        this.agendamentoService = agendamentoService;
        this.exameService = exameService;
    }

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.MENU_PRINCIPAL);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        return switch (texto) {
            case "1" -> iniciarAgendamento(sessao);
            case "2" -> listarAgendamentos(sessao);
            case "3" -> listarExames(sessao);
            case "4" -> obterInformacoesGerais();
            case "5" -> transferirParaHumano(sessao);
            case "6" -> {
                sessao.finalizar();
                yield gerarMensagemDespedida();
            }
            default -> "Opção não reconhecida. Por favor, digite um número de *1 a 6*, ou digite *menu*.";
        };
    }

    private String iniciarAgendamento(SessaoAtendimento sessao) {
        sessao.iniciarEscolhaEspecialidade();
        List<Especialidade> especialidades = especialidadeRepository.findAll();
        StringBuilder sb = new StringBuilder("🩺 *Selecione a Especialidade Desejada:*\n\n");
        for (int i = 0; i < especialidades.size(); i++) {
            sb.append(i + 1).append("️⃣ ").append(especialidades.get(i).getNome()).append("\n");
        }
        sb.append("\n👉 _Digite o número correspondente (ou 'menu' para voltar):_");
        return sb.toString();
    }

    private String listarAgendamentos(SessaoAtendimento sessao) {
        sessao.iniciarConsultaAgendamentos();
        List<Agendamento> agendamentos = agendamentoService.listarEntidadesPorPaciente(sessao.getPaciente().getId());
        if (agendamentos.isEmpty()) {
            sessao.retornarAoMenuPrincipal();
            return "ℹ️ Você não possui agendamentos ativos no momento.\n\nDigite *1* para marcar uma consulta ou *menu* para voltar.";
        }
        return "📅 *Seus Agendamentos:*\n\n"
                + formatarRegistrosClinicos(agendamentos, true)
                + "Para cancelar algum agendamento, digite *cancelar <número do item>*\nOu digite *menu* para voltar ao menu principal.";
    }

    private String listarExames(SessaoAtendimento sessao) {
        sessao.iniciarConsultaExames();
        List<Exame> exames = exameService.listarPorPaciente(sessao.getPaciente().getId());
        if (exames.isEmpty()) {
            sessao.retornarAoMenuPrincipal();
            return "🔬 Não foram encontrados exames recentes vinculados ao seu cadastro.\n\nDigite *menu* para retornar ao início.";
        }
        return "🔬 *Seus Exames e Laudos:*\n\n"
                + formatarRegistrosClinicos(exames, false)
                + "👉 _Digite 'menu' para voltar ao menu principal ou 'encerrar' para finalizar._";
    }

    private String obterInformacoesGerais() {
        return "🏥 *OmniMed Care - Informações Gerais*\n\n"
                + "📍 *Endereço:* Av. Paulista, 1500 - 8º Andar, São Paulo - SP\n"
                + "🕒 *Horário de Atendimento:* Seg a Sex das 07h às 19h | Sáb das 08h às 13h\n"
                + "💳 *Convênios Aceitos:* Unimed, Bradesco Saúde, SulAmérica, Amil, NotreDame Intermédica e Particular.\n"
                + "📞 *Telefone Central:* (11) 4002-8922\n\n"
                + "👉 _Digite 'menu' para voltar ao início ou 'encerrar' para finalizar._";
    }

    private String transferirParaHumano(SessaoAtendimento sessao) {
        sessao.transbordarParaAtendente();
        return "👤 *Transferindo para Atendimento Humano...*\n\n"
                + "Você foi colocado(a) na fila de prioridade da nossa recepção.\n"
                + "Um atendente humano assumirá esta conversa em breve aqui mesmo neste canal.\n\n"
                + "_Por favor, descreva detalhadamente sua solicitação enquanto aguarda._";
    }
}
