package com.omnimed.crm.repository;

import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.model.StatusSessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessaoAtendimentoRepository extends JpaRepository<SessaoAtendimento, Long> {
    Optional<SessaoAtendimento> findByCanalAndCanalUsuarioId(CanalTipo canal, String canalUsuarioId);
    Optional<SessaoAtendimento> findTopByCanalAndCanalUsuarioIdOrderByDataUltimaInteracaoDesc(CanalTipo canal, String canalUsuarioId);
    Optional<SessaoAtendimento> findTopByCanalAndCanalUsuarioIdAndStatusNotOrderByDataUltimaInteracaoDesc(CanalTipo canal, String canalUsuarioId, StatusSessao status);
    List<SessaoAtendimento> findByStatusOrderByDataUltimaInteracaoDesc(StatusSessao status);
    List<SessaoAtendimento> findByStatusInOrderByDataUltimaInteracaoDesc(List<StatusSessao> statuses);
    List<SessaoAtendimento> findByStatusInAndDataUltimaInteracaoBefore(List<StatusSessao> statuses, LocalDateTime cutoff);
    long countByStatus(StatusSessao status);
}
