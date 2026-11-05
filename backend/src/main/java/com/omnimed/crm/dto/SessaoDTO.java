package com.omnimed.crm.dto;

import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.StatusSessao;
import java.time.LocalDateTime;

public record SessaoDTO(
    Long id,
    CanalTipo canal,
    String canalUsuarioId,
    Long pacienteId,
    String pacienteNome,
    String pacienteCpf,
    String pacienteTelefone,
    StatusSessao status,
    EstadoConversa estadoConversa,
    LocalDateTime dataInicio,
    LocalDateTime dataUltimaInteracao,
    String ultimaMensagem
) {}
