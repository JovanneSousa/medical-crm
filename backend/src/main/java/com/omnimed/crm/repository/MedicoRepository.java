package com.omnimed.crm.repository;

import com.omnimed.crm.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    List<Medico> findByEspecialidadeIdAndAtivoTrue(Long especialidadeId);
    List<Medico> findByAtivoTrue();
    Optional<Medico> findByCrm(String crm);
}
