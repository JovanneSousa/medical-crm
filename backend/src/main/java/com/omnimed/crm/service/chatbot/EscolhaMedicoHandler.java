package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.HorarioAtendimento;
import com.omnimed.crm.model.Medico;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.repository.MedicoRepository;
import com.omnimed.crm.service.AgendamentoService;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Handler responsável pela seleção do médico e exibição dos horários disponíveis.
 */
@Component
public class EscolhaMedicoHandler extends AbstractEstadoConversaHandler {

    private static final DateTimeFormatter DIA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM (EEEE)");

    private final MedicoRepository medicoRepository;
    private final AgendamentoService agendamentoService;

    public EscolhaMedicoHandler(MedicoRepository medicoRepository, AgendamentoService agendamentoService) {
        this.medicoRepository = medicoRepository;
        this.agendamentoService = agendamentoService;
    }

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.ESCOLHENDO_MEDICO);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        List<Medico> medicos = medicoRepository.findByEspecialidadeIdAndAtivoTrue(sessao.getTempEspecialidadeId());
        Optional<Integer> indiceOpt = parsearIndiceEscolhido(texto, medicos.size());
        if (indiceOpt.isEmpty()) {
            return "Por favor, digite o número do médico desejado (ou *menu* para voltar).";
        }

        Medico medico = medicos.get(indiceOpt.get());
        sessao.selecionarMedico(medico.getId());

        List<HorarioAtendimento> horarios = agendamentoService.listarHorariosDisponiveis(medico.getId());
        if (horarios.isEmpty()) {
            sessao.retornarAoMenuPrincipal();
            return "ℹ️ O(A) Dr(a). " + medico.getNome() + " não possui horários livres nos próximos dias.\nDigite *menu* para tentar outro médico.";
        }

        StringBuilder sb = new StringBuilder("⏰ *Horários Livres com Dr(a). " + medico.getNome() + ":*\n\n");
        int limit = Math.min(horarios.size(), 8);
        for (int i = 0; i < limit; i++) {
            HorarioAtendimento h = horarios.get(i);
            sb.append(i + 1).append("️⃣ ")
              .append(h.getData().format(DIA_FORMATTER))
              .append(" às ").append(h.getHoraInicio()).append("\n");
        }
        sb.append("\n👉 _Digite o número do horário que prefere:_");
        return sb.toString();
    }
}
