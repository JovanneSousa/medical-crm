package com.omnimed.crm.dto;

import com.omnimed.crm.model.RemetenteTipo;
import java.time.LocalDateTime;

public record MensagemDTO(
    Long id,
    Long sessaoId,
    RemetenteTipo remetente,
    String conteudo,
    LocalDateTime dataHora,
    Boolean lida
) {}
