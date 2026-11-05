package com.omnimed.crm.controller;

import com.omnimed.crm.model.Especialidade;
import com.omnimed.crm.model.HorarioAtendimento;
import com.omnimed.crm.model.Medico;
import com.omnimed.crm.repository.EspecialidadeRepository;
import com.omnimed.crm.repository.MedicoRepository;
import com.omnimed.crm.service.AgendamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MedicoController {

    private final MedicoRepository medicoRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final AgendamentoService agendamentoService;

    public MedicoController(MedicoRepository medicoRepository,
                            EspecialidadeRepository especialidadeRepository,
                            AgendamentoService agendamentoService) {
        this.medicoRepository = medicoRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/medicos")
    public ResponseEntity<List<Medico>> listarMedicos() {
        return ResponseEntity.ok(medicoRepository.findByAtivoTrue());
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
