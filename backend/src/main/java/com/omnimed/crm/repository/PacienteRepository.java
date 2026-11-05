package com.omnimed.crm.repository;

import com.omnimed.crm.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByCpf(String cpf);
    Optional<Paciente> findByTelegramChatId(String telegramChatId);
    boolean existsByCpf(String cpf);
}
