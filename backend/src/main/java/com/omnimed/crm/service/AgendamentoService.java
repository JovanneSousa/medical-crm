package com.omnimed.crm.service;

import com.omnimed.crm.dto.AgendamentoDTO;
import com.omnimed.crm.model.Agendamento;
import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.model.HorarioAtendimento;
import com.omnimed.crm.model.Medico;
import com.omnimed.crm.model.Paciente;
import com.omnimed.crm.repository.AgendamentoRepository;
import com.omnimed.crm.repository.HorarioAtendimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final HorarioAtendimentoRepository horarioRepository;
    private final Random random = new Random();

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              HorarioAtendimentoRepository horarioRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.horarioRepository = horarioRepository;
    }

    public List<AgendamentoDTO> listarTodos() {
        return agendamentoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<AgendamentoDTO> listarPorPaciente(Long pacienteId) {
        return listarEntidadesPorPaciente(pacienteId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<Agendamento> listarEntidadesPorPaciente(Long pacienteId) {
        return agendamentoRepository.findByPacienteIdOrderByDataHoraDesc(pacienteId);
    }

    public List<HorarioAtendimento> listarHorariosDisponiveis(Long medicoId) {
        return horarioRepository.findByMedicoIdAndDataGreaterThanEqualAndDisponivelTrueOrderByDataAscHoraInicioAsc(
                medicoId, LocalDate.now());
    }

    @Transactional
    public Agendamento criarAgendamento(Paciente paciente, Medico medico, HorarioAtendimento horario, CanalTipo canalOrigem) {
        horario.reservar();

        String protocolo = "OM-" + (10000 + random.nextInt(90000));
        Agendamento agendamento = new Agendamento(
                protocolo,
                paciente,
                medico,
                horario.getDataHoraInicio(),
                canalOrigem,
                "Agendamento via canal " + canalOrigem
        );

        horarioRepository.save(horario);
        return agendamentoRepository.save(agendamento);
    }

    @Transactional
    public Agendamento cancelarAgendamento(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com id: " + id));

        agendamento.cancelar();

        LocalDate data = agendamento.getDataHora().toLocalDate();
        LocalTime hora = agendamento.getDataHora().toLocalTime();

        Optional<HorarioAtendimento> slotOpt = horarioRepository.findByMedicoIdAndDataAndHoraInicio(
                agendamento.getMedico().getId(), data, hora
        );

        if (slotOpt.isPresent()) {
            HorarioAtendimento slot = slotOpt.get();
            slot.liberar();
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
