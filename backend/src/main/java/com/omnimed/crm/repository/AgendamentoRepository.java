package com.omnimed.crm.repository;

import com.omnimed.crm.model.Agendamento;
import com.omnimed.crm.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByPacienteIdOrderByDataHoraDesc(Long pacienteId);
    List<Agendamento> findByMedicoIdOrderByDataHoraAsc(Long medicoId);
    List<Agendamento> findByDataHoraBetweenOrderByDataHoraAsc(LocalDateTime inicio, LocalDateTime fim);
    Optional<Agendamento> findByProtocolo(String protocolo);
    long countByStatus(StatusAgendamento status);

    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.status = 'CONFIRMADO'")
    long countConfirmados();
}
