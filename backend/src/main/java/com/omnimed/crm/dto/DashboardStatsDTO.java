package com.omnimed.crm.dto;

public record DashboardStatsDTO(
    long totalAgendamentos,
    long agendamentosConfirmados,
    long atendimentosAtivos,
    long aguardandoHumano,
    long totalPacientes,
    double taxaRetencaoBot,
    long totalMedicos
) {}
