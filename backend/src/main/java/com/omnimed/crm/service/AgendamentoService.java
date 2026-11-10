package com.omnimed.crm.service;

import com.omnimed.crm.dto.AgendamentoDTO;
import com.omnimed.crm.model.*;
import com.omnimed.crm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final HorarioAtendimentoRepository horarioRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              HorarioAtendimentoRepository horarioRepository,
                              MedicoRepository medicoRepository,
                              PacienteRepository pacienteRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.horarioRepository = horarioRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    public List<AgendamentoDTO> listarTodos() {
        return agendamentoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<AgendamentoDTO> listarPorPaciente(Long pacienteId) {
        return agendamentoRepository.findByPacienteIdOrderByDataHoraDesc(pacienteId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<HorarioAtendimento> listarHorariosDisponiveis(Long medicoId) {
        return horarioRepository.findByMedicoIdAndDataGreaterThanEqualAndDisponivelTrueOrderByDataAscHoraInicioAsc(
                medicoId, LocalDate.now());
    }

    @Transactional
    public Agendamento criarAgendamento(Paciente paciente, Medico medico, HorarioAtendimento horario, CanalTipo canalOrigem) {
        if (!horario.getDisponivel()) {
            throw new IllegalStateException("O horário selecionado não está mais disponível.");
        }

        LocalDateTime dataHora = LocalDateTime.of(horario.getData(), horario.getHoraInicio());
        String protocolo = "OM-" + (10000 + new Random().nextInt(90000));

        Agendamento agendamento = new Agendamento(
                protocolo,
                paciente,
                medico,
                dataHora,
                canalOrigem,
                "Agendamento via canal " + canalOrigem
        );

        horario.setDisponivel(false);
        horarioRepository.save(horario);

        return agendamentoRepository.save(agendamento);
    }

    @Transactional
    public Agendamento cancelarAgendamento(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com id: " + id));

        agendamento.setStatus(StatusAgendamento.CANCELADO);

        // Reativa o slot na agenda clínica
        LocalDate data = agendamento.getDataHora().toLocalDate();
        LocalTime hora = agendamento.getDataHora().toLocalTime();

        Optional<HorarioAtendimento> slotOpt = horarioRepository.findByMedicoIdAndDataAndHoraInicio(
                agendamento.getMedico().getId(), data, hora
        );

        if (slotOpt.isPresent()) {
            HorarioAtendimento slot = slotOpt.get();
            slot.setDisponivel(true);
            horarioRepository.save(slot);
        } else {
            horarioRepository.save(new HorarioAtendimento(agendamento.getMedico(), data, hora, hora.plusMinutes(30)));
        }

        return agendamentoRepository.save(agendamento);
    }

    public AgendamentoDTO toDTO(Agendamento a) {
        return new AgendamentoDTO(
                a.getId(),
                a.getProtocolo(),
                a.getPaciente().getId(),
                a.getPaciente().getNome(),
                a.getPaciente().getCpf(),
                a.getPaciente().getTelefone(),
                a.getMedico().getId(),
                a.getMedico().getNome(),
                a.getMedico().getEspecialidade().getNome(),
                a.getDataHora(),
                a.getStatus(),
                a.getCanalOrigem(),
                a.getObservacoes(),
                a.getCriadoEm()
        );
    }
}
