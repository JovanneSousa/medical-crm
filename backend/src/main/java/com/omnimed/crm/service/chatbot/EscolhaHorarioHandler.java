package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.Agendamento;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.HorarioAtendimento;
import com.omnimed.crm.model.Medico;
import com.omnimed.crm.model.Paciente;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.repository.MedicoRepository;
import com.omnimed.crm.service.AgendamentoService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Handler responsável pela confirmação do horário e criação do agendamento.
 */
@Component
public class EscolhaHorarioHandler extends AbstractEstadoConversaHandler {

    private final AgendamentoService agendamentoService;
    private final MedicoRepository medicoRepository;

    public EscolhaHorarioHandler(AgendamentoService agendamentoService, MedicoRepository medicoRepository) {
        this.agendamentoService = agendamentoService;
        this.medicoRepository = medicoRepository;
    }

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.ESCOLHENDO_HORARIO);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        List<HorarioAtendimento> horarios = agendamentoService.listarHorariosDisponiveis(sessao.getTempMedicoId());
        Optional<Integer> indiceOpt = parsearIndiceEscolhido(texto, horarios.size());
        if (indiceOpt.isEmpty()) {
            return "Opção de horário inválida. Por favor, selecione um número da lista ou digite *menu*.";
        }

        HorarioAtendimento horarioEscolhido = horarios.get(indiceOpt.get());
        Medico medico = medicoRepository.findById(sessao.getTempMedicoId()).orElseThrow();
        Paciente paciente = sessao.getPaciente();

        Agendamento agendamento = agendamentoService.criarAgendamento(
                paciente, medico, horarioEscolhido, canal.getTipoCanal()
        );
        sessao.retornarAoMenuPrincipal();

        return "🎉 *CONSULTA AGENDADA COM SUCESSO!*\n\n"
                + "📌 *Protocolo:* `" + agendamento.getCodigoIdentificador() + "`\n"
                + "👤 *Paciente:* " + paciente.getNome() + "\n"
                + "🩺 *Especialidade:* " + medico.getEspecialidade().getNome() + "\n"
                + "👨‍⚕️ *Médico:* " + medico.getNome() + "\n"
                + "🗓️ *Data e Hora:* " + agendamento.getDataHora().format(DATE_FORMATTER) + "\n"
                + "📍 *Local:* Av. Paulista, 1500 - 8º Andar\n\n"
                + "Enviaremos lembrete antes do horário. Obrigado por escolher a OmniMed Care!\n\n"
                + "👉 _Digite 'menu' para voltar ao início ou 'encerrar' para finalizar o atendimento._";
    }
}
