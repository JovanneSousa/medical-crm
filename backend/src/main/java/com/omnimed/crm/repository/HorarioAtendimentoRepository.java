package com.omnimed.crm.repository;

import com.omnimed.crm.model.HorarioAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioAtendimentoRepository extends JpaRepository<HorarioAtendimento, Long> {
    List<HorarioAtendimento> findByMedicoIdAndDataGreaterThanEqualAndDisponivelTrueOrderByDataAscHoraInicioAsc(Long medicoId, LocalDate data);
    List<HorarioAtendimento> findByMedicoIdAndDataAndDisponivelTrueOrderByHoraInicioAsc(Long medicoId, LocalDate data);
    Optional<HorarioAtendimento> findByMedicoIdAndDataAndHoraInicio(Long medicoId, LocalDate data, LocalTime horaInicio);
}
