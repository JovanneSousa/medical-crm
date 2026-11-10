package com.omnimed.crm.controller;

import com.omnimed.crm.dto.PacienteDTO;
import com.omnimed.crm.dto.PacienteRequestDTO;
import com.omnimed.crm.model.Paciente;
import com.omnimed.crm.repository.AgendamentoRepository;
import com.omnimed.crm.repository.PacienteRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteRepository pacienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    public PacienteController(PacienteRepository pacienteRepository,
                              AgendamentoRepository agendamentoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @GetMapping
    public ResponseEntity<List<PacienteDTO>> listar(@RequestParam(required = false) String busca) {
        List<Paciente> lista;
        if (busca != null && !busca.isBlank()) {
            String termo = busca.trim();
            lista = pacienteRepository.findByNomeContainingIgnoreCaseOrCpfContaining(termo, termo);
        } else {
            lista = pacienteRepository.findAll();
        }

        List<PacienteDTO> dtos = lista.stream().map(p -> {
            long totalAgendamentos = agendamentoRepository.findByPacienteIdOrderByDataHoraDesc(p.getId()).size();
            return new PacienteDTO(
                    p.getId(),
                    p.getNome(),
                    p.getCpf(),
                    p.getTelefone(),
                    p.getEmail(),
                    p.getTelegramChatId(),
                    p.getWhatsappNumber(),
                    p.getDataCadastro(),
                    totalAgendamentos
            );
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteDTO> buscarPorId(@PathVariable Long id) {
        return pacienteRepository.findById(id).map(p -> {
            long totalAgendamentos = agendamentoRepository.findByPacienteIdOrderByDataHoraDesc(p.getId()).size();
            return ResponseEntity.ok(new PacienteDTO(
                    p.getId(),
                    p.getNome(),
                    p.getCpf(),
                    p.getTelefone(),
                    p.getEmail(),
                    p.getTelegramChatId(),
                    p.getWhatsappNumber(),
                    p.getDataCadastro(),
                    totalAgendamentos
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(@Valid @RequestBody PacienteRequestDTO dto) {
        String cpfLimpo = dto.cpf().replaceAll("[^0-9]", "");
        if (cpfLimpo.length() != 11) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "CPF deve conter exatamente 11 dígitos."));
        }

        if (pacienteRepository.findByCpf(cpfLimpo).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensagem", "Já existe um paciente cadastrado com este CPF: " + cpfLimpo));
        }

        Paciente paciente = new Paciente(
                dto.nome().trim(),
                cpfLimpo,
                dto.telefone() != null ? dto.telefone().trim() : "",
                dto.email() != null ? dto.email().trim() : "",
                dto.telegramChatId() != null && !dto.telegramChatId().isBlank() ? dto.telegramChatId().trim() : null,
                dto.whatsappNumber() != null && !dto.whatsappNumber().isBlank() ? dto.whatsappNumber().replaceAll("[^0-9]", "") : null
        );

        paciente = pacienteRepository.save(paciente);

        return ResponseEntity.status(HttpStatus.CREATED).body(new PacienteDTO(
                paciente.getId(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getTelefone(),
                paciente.getEmail(),
                paciente.getTelegramChatId(),
                paciente.getWhatsappNumber(),
                paciente.getDataCadastro(),
                0
        ));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequestDTO dto) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com id: " + id));

        String cpfLimpo = dto.cpf().replaceAll("[^0-9]", "");
        if (cpfLimpo.length() != 11) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "CPF deve conter exatamente 11 dígitos."));
        }

        if (pacienteRepository.existsByCpfAndIdNot(cpfLimpo, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensagem", "CPF já cadastrado para outro paciente: " + cpfLimpo));
        }

        paciente.setNome(dto.nome().trim());
        paciente.setCpf(cpfLimpo);
        paciente.setTelefone(dto.telefone() != null ? dto.telefone().trim() : "");
        paciente.setEmail(dto.email() != null ? dto.email().trim() : "");
        paciente.setTelegramChatId(dto.telegramChatId() != null && !dto.telegramChatId().isBlank() ? dto.telegramChatId().trim() : null);
        paciente.setWhatsappNumber(dto.whatsappNumber() != null && !dto.whatsappNumber().isBlank() ? dto.whatsappNumber().replaceAll("[^0-9]", "") : null);

        paciente = pacienteRepository.save(paciente);
        long totalAgendamentos = agendamentoRepository.findByPacienteIdOrderByDataHoraDesc(paciente.getId()).size();

        return ResponseEntity.ok(new PacienteDTO(
                paciente.getId(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getTelefone(),
                paciente.getEmail(),
                paciente.getTelegramChatId(),
                paciente.getWhatsappNumber(),
                paciente.getDataCadastro(),
                totalAgendamentos
        ));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com id: " + id));

        long totalAgendamentos = agendamentoRepository.findByPacienteIdOrderByDataHoraDesc(id).size();
        if (totalAgendamentos > 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem", "Não é possível excluir o paciente pois existem " + totalAgendamentos + " agendamento(s) vinculado(s) ao seu histórico."
            ));
        }

        pacienteRepository.delete(paciente);
        return ResponseEntity.noContent().build();
    }
}
