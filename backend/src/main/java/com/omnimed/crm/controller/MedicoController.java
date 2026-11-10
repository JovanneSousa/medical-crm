package com.omnimed.crm.controller;

import com.omnimed.crm.dto.MedicoRequestDTO;
import com.omnimed.crm.model.Especialidade;
import com.omnimed.crm.model.HorarioAtendimento;
import com.omnimed.crm.model.Medico;
import com.omnimed.crm.repository.AgendamentoRepository;
import com.omnimed.crm.repository.EspecialidadeRepository;
import com.omnimed.crm.repository.HorarioAtendimentoRepository;
import com.omnimed.crm.repository.MedicoRepository;
import com.omnimed.crm.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MedicoController {

    private final MedicoRepository medicoRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final HorarioAtendimentoRepository horarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoService agendamentoService;

    public MedicoController(MedicoRepository medicoRepository,
                            EspecialidadeRepository especialidadeRepository,
                            HorarioAtendimentoRepository horarioRepository,
                            AgendamentoRepository agendamentoRepository,
                            AgendamentoService agendamentoService) {
        this.medicoRepository = medicoRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.horarioRepository = horarioRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/medicos")
    public ResponseEntity<List<Medico>> listarMedicos(@RequestParam(required = false) Boolean apenasAtivos) {
        if (Boolean.TRUE.equals(apenasAtivos)) {
            return ResponseEntity.ok(medicoRepository.findByAtivoTrue());
        }
        return ResponseEntity.ok(medicoRepository.findAll());
    }

    @GetMapping("/medicos/{id}")
    public ResponseEntity<Medico> buscarPorId(@PathVariable Long id) {
        return medicoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/medicos")
    @Transactional
    public ResponseEntity<?> criarMedico(@Valid @RequestBody MedicoRequestDTO dto) {
        if (medicoRepository.findByCrm(dto.crm()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Já existe um médico cadastrado com este CRM: " + dto.crm());
        }

        Especialidade especialidade = especialidadeRepository.findById(dto.especialidadeId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada com id: " + dto.especialidadeId()));

        Medico medico = new Medico(
                dto.nome(),
                dto.crm(),
                especialidade,
                dto.email(),
                dto.telefone()
        );
        if (dto.ativo() != null) {
            medico.setAtivo(dto.ativo());
        }

        medico = medicoRepository.save(medico);

        // Gera slots de atendimento padrão para os próximos 5 dias úteis
        LocalDate hoje = LocalDate.now();
        for (int d = 0; d <= 4; d++) {
            LocalDate data = hoje.plusDays(d);
            horarioRepository.save(new HorarioAtendimento(medico, data, LocalTime.of(9, 0), LocalTime.of(9, 30)));
            horarioRepository.save(new HorarioAtendimento(medico, data, LocalTime.of(10, 30), LocalTime.of(11, 0)));
            horarioRepository.save(new HorarioAtendimento(medico, data, LocalTime.of(14, 0), LocalTime.of(14, 30)));
            horarioRepository.save(new HorarioAtendimento(medico, data, LocalTime.of(16, 0), LocalTime.of(16, 30)));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(medico);
    }

    @PutMapping("/medicos/{id}")
    @Transactional
    public ResponseEntity<?> atualizarMedico(@PathVariable Long id, @Valid @RequestBody MedicoRequestDTO dto) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com id: " + id));

        medicoRepository.findByCrm(dto.crm()).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new IllegalArgumentException("CRM já cadastrado para outro profissional: " + dto.crm());
            }
        });

        Especialidade especialidade = especialidadeRepository.findById(dto.especialidadeId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada com id: " + dto.especialidadeId()));

        medico.setNome(dto.nome());
        medico.setCrm(dto.crm());
        medico.setEspecialidade(especialidade);
        medico.setEmail(dto.email());
        medico.setTelefone(dto.telefone());
        if (dto.ativo() != null) {
            medico.setAtivo(dto.ativo());
        }

        return ResponseEntity.ok(medicoRepository.save(medico));
    }

    @DeleteMapping("/medicos/{id}")
    @Transactional
    public ResponseEntity<?> excluirMedico(@PathVariable Long id) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com id: " + id));

        // Se houver agendamentos associados a este médico, inativa para manter integridade histórica
        boolean temAgendamentos = !agendamentoRepository.findByMedicoIdOrderByDataHoraAsc(id).isEmpty();
        if (temAgendamentos) {
            medico.setAtivo(false);
            medicoRepository.save(medico);
            return ResponseEntity.ok(java.util.Map.of(
                    "mensagem", "Médico possui histórico de consultas e foi inativado com sucesso.",
                    "ativo", false
            ));
        }

        medicoRepository.delete(medico);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medicos/{id}/horarios")
    public ResponseEntity<List<HorarioAtendimento>> listarHorarios(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.listarHorariosDisponiveis(id));
    }

    @GetMapping("/especialidades")
    public ResponseEntity<List<Especialidade>> listarEspecialidades() {
        return ResponseEntity.ok(especialidadeRepository.findAll());
    }
}
