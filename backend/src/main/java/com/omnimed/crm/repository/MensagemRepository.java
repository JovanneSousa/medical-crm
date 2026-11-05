package com.omnimed.crm.repository;

import com.omnimed.crm.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findBySessaoIdOrderByDataHoraAsc(Long sessaoId);
    List<Mensagem> findTop50BySessaoIdOrderByDataHoraDesc(Long sessaoId);
}
