package com.omnimed.crm.dto;

import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.model.StatusAgendamento;
import java.time.LocalDateTime;

public record AgendamentoDTO(
    Long id,
    String protocolo,
    Long pacienteId,
    String pacienteNome,
    String pacienteCpf,
    String pacienteTelefone,
    Long medicoId,
    String medicoNome,
    String especialidadeNome,
    LocalDateTime dataHora,
    StatusAgendamento status,
    CanalTipo canalOrigem,
    String observacoes,
    LocalDateTime criadoEm
) {}
