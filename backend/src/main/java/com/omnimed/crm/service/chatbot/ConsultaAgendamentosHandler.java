package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.dto.AgendamentoDTO;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.service.AgendamentoService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Handler responsável pela consulta e cancelamento de agendamentos pelo paciente.
 */
@Component
public class ConsultaAgendamentosHandler extends AbstractEstadoConversaHandler {

    private final AgendamentoService agendamentoService;

    public ConsultaAgendamentosHandler(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.CONSULTANDO_AGENDAMENTOS);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        if (texto.toLowerCase().startsWith("cancelar")) {
            String[] parts = texto.split(" ");
            if (parts.length >= 2) {
                List<AgendamentoDTO> lista = agendamentoService.listarPorPaciente(sessao.getPaciente().getId());
                Optional<Integer> indexOpt = parsearIndiceEscolhido(parts[1], lista.size());
                if (indexOpt.isPresent()) {
                    try {
                        agendamentoService.cancelarAgendamento(lista.get(indexOpt.get()).id());
                        sessao.retornarAoMenuPrincipal();
                        return "✅ Agendamento cancelado com sucesso. O horário foi liberado em nossa agenda.\n\nDigite *menu* para voltar ou *encerrar* para finalizar.";
                    } catch (IllegalStateException e) {
                        return "⚠️ " + e.getMessage() + "\n\nDigite *menu* para voltar.";
                    }
                }
            }
            return "Formato incorreto. Use: *cancelar <número>* (ex: cancelar 1) ou *menu* para voltar.";
        }

        sessao.retornarAoMenuPrincipal();
        return gerarMenuPrincipal(sessao.getPaciente().getNome());
    }
}
