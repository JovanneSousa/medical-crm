package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.Especialidade;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.Medico;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.repository.EspecialidadeRepository;
import com.omnimed.crm.repository.MedicoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Handler responsável pela seleção de especialidade médica no fluxo de agendamento.
 */
@Component
public class EscolhaEspecialidadeHandler extends AbstractEstadoConversaHandler {

    private final EspecialidadeRepository especialidadeRepository;
    private final MedicoRepository medicoRepository;

    public EscolhaEspecialidadeHandler(EspecialidadeRepository especialidadeRepository,
                                       MedicoRepository medicoRepository) {
        this.especialidadeRepository = especialidadeRepository;
        this.medicoRepository = medicoRepository;
    }

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.ESCOLHENDO_ESPECIALIDADE);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        List<Especialidade> especialidades = especialidadeRepository.findAll();
        Optional<Integer> indiceOpt = parsearIndiceEscolhido(texto, especialidades.size());
        if (indiceOpt.isEmpty()) {
            return "Por favor, digite o número válido da especialidade listada (ou *menu* para voltar).";
        }

        Especialidade esp = especialidades.get(indiceOpt.get());
        sessao.selecionarEspecialidade(esp.getId());

        List<Medico> medicos = medicoRepository.findByEspecialidadeIdAndAtivoTrue(esp.getId());
        if (medicos.isEmpty()) {
            sessao.retornarAoMenuPrincipal();
            return "ℹ️ Nenhum médico ativo encontrado para a especialidade " + esp.getNome() + ".\nDigite *menu* para voltar.";
        }

        StringBuilder sb = new StringBuilder("👨‍⚕️ *Médicos Disponíveis em " + esp.getNome() + ":*\n\n");
        for (int i = 0; i < medicos.size(); i++) {
            Medico medico = medicos.get(i);
            sb.append(i + 1).append("️⃣ ").append(medico.getNome())
              .append(" (CRM: ").append(medico.getIdentificacaoPrincipal()).append(")\n");
        }
        sb.append("\n👉 _Digite o número do médico desejado:_");
        return sb.toString();
    }
}
