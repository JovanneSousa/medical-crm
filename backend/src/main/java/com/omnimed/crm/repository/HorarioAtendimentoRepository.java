package com.omnimed.crm.repository;

import com.omnimed.crm.model.HorarioAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HorarioAtendimentoRepository extends JpaRepository<HorarioAtendimento, Long> {
    List<HorarioAtendimento> findByMedicoIdAndDataGreaterThanEqualAndDisponivelTrueOrderByDataAscHoraInicioAsc(Long medicoId, LocalDate data);
    List<HorarioAtendimento> findByMedicoIdAndDataAndDisponivelTrueOrderByHoraInicioAsc(Long medicoId, LocalDate data);
}
