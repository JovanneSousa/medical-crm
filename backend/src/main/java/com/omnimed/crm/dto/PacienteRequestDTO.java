package com.omnimed.crm.dto;

import jakarta.validation.constraints.NotBlank;

public record PacienteRequestDTO(
        @NotBlank(message = "O nome do paciente é obrigatório")
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        String cpf,

        String telefone,
        String email,
        String telegramChatId,
        String whatsappNumber
) {}
