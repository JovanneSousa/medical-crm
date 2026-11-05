package com.omnimed.crm.controller;

import com.omnimed.crm.dto.DashboardStatsDTO;
import com.omnimed.crm.model.StatusAgendamento;
import com.omnimed.crm.model.StatusSessao;
import com.omnimed.crm.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AgendamentoRepository agendamentoRepository;
    private final SessaoAtendimentoRepository sessaoRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    public DashboardController(AgendamentoRepository agendamentoRepository,
                               SessaoAtendimentoRepository sessaoRepository,
                               PacienteRepository pacienteRepository,
                               MedicoRepository medicoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.sessaoRepository = sessaoRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        long totalAgendamentos = agendamentoRepository.count();
        long confirmados = agendamentoRepository.countByStatus(StatusAgendamento.CONFIRMADO);
        long aguardandoHumano = sessaoRepository.countByStatus(StatusSessao.AGUARDANDO_HUMANO);
        long emAtendimento = sessaoRepository.countByStatus(StatusSessao.EM_ATENDIMENTO_HUMANO);
        long totalSessoes = sessaoRepository.count();
        long totalPacientes = pacienteRepository.count();
        long totalMedicos = medicoRepository.count();

        // Taxa de retenção do bot (% de conversas que foram resolvidas sem transbordo)
        double taxaRetencao = totalSessoes > 0 
                ? ((double) (totalSessoes - (aguardandoHumano + emAtendimento)) / totalSessoes) * 100.0 
                : 85.0;

        return ResponseEntity.ok(new DashboardStatsDTO(
                totalAgendamentos,
                confirmados,
                emAtendimento,
                aguardandoHumano,
                totalPacientes,
                Math.round(taxaRetencao * 10.0) / 10.0,
                totalMedicos
        ));
    }
}
