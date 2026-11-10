package com.omnimed.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicoRequestDTO(
        @NotBlank(message = "O nome do médico é obrigatório")
        String nome,

        @NotBlank(message = "O CRM é obrigatório")
        String crm,

        @NotNull(message = "A especialidade é obrigatória")
        Long especialidadeId,

        String email,
        String telefone,
        Boolean ativo
) {}
