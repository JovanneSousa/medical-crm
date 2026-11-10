package com.omnimed.crm.dto;

import java.time.LocalDateTime;

public record PacienteDTO(
        Long id,
        String nome,
        String cpf,
        String telefone,
        String email,
        String telegramChatId,
        String whatsappNumber,
        LocalDateTime dataCadastro,
        long totalAgendamentos
) {}
