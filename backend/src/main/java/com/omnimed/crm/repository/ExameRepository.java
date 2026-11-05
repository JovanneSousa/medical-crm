package com.omnimed.crm.repository;

import com.omnimed.crm.model.Exame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExameRepository extends JpaRepository<Exame, Long> {
    List<Exame> findByPacienteIdOrderByDataColetaDesc(Long pacienteId);
    Optional<Exame> findByCodigoExame(String codigoExame);
}
