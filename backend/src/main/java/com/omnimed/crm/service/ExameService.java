package com.omnimed.crm.service;

import com.omnimed.crm.model.Exame;
import com.omnimed.crm.repository.ExameRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExameService {

    private final ExameRepository exameRepository;

    public ExameService(ExameRepository exameRepository) {
        this.exameRepository = exameRepository;
    }

    public List<Exame> listarPorPaciente(Long pacienteId) {
        return exameRepository.findByPacienteIdOrderByDataColetaDesc(pacienteId);
    }

    public Optional<Exame> buscarPorCodigo(String codigo) {
        return exameRepository.findByCodigoExame(codigo);
    }
}
