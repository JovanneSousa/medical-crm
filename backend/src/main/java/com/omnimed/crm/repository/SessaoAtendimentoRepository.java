package com.omnimed.crm.repository;

import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.model.StatusSessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessaoAtendimentoRepository extends JpaRepository<SessaoAtendimento, Long> {
    Optional<SessaoAtendimento> findByCanalAndCanalUsuarioId(CanalTipo canal, String canalUsuarioId);
    List<SessaoAtendimento> findByStatusOrderByDataUltimaInteracaoDesc(StatusSessao status);
    List<SessaoAtendimento> findByStatusInOrderByDataUltimaInteracaoDesc(List<StatusSessao> statuses);
    long countByStatus(StatusSessao status);
}
