package com.omnimed.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa um agendamento de consulta médica.
 * Herda de {@link RegistroClinico} e encapsula as transições de status da consulta.
 */
@Entity
@Table(name = "agendamentos")
public class Agendamento extends RegistroClinico {

    private static final DateTimeFormatter CHAT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Column(nullable = false, unique = true, length = 30)
    private String protocolo;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusAgendamento status;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origem", nullable = false, length = 30)
    private CanalTipo canalOrigem;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Agendamento() {
        this.criadoEm = LocalDateTime.now();
        this.status = StatusAgendamento.CONFIRMADO;
    }

    public Agendamento(String protocolo, Paciente paciente, Medico medico, LocalDateTime dataHora, CanalTipo canalOrigem, String observacoes) {
        super(paciente, observacoes);
        setProtocolo(protocolo);
        setMedico(medico);
        setDataHora(dataHora);
        setCanalOrigem(canalOrigem);
        this.status = StatusAgendamento.CONFIRMADO;
        this.criadoEm = LocalDateTime.now();
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        if (protocolo == null || protocolo.isBlank()) {
            throw new IllegalArgumentException("O protocolo do agendamento é obrigatório.");
        }
        this.protocolo = protocolo.trim();
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        if (medico == null) {
            throw new IllegalArgumentException("O médico do agendamento é obrigatório.");
        }
        this.medico = medico;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        if (dataHora == null) {
            throw new IllegalArgumentException("A data e hora do agendamento são obrigatórias.");
        }
        this.dataHora = dataHora;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public CanalTipo getCanalOrigem() {
        return canalOrigem;
    }

    public void setCanalOrigem(CanalTipo canalOrigem) {
        this.canalOrigem = canalOrigem;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    /**
     * Encapsula a regra de cancelamento de um agendamento.
     */
    public void cancelar() {
        if (this.status == StatusAgendamento.CANCELADO) {
            throw new IllegalStateException("Este agendamento já foi cancelado anteriormente.");
        }
        if (this.status == StatusAgendamento.REALIZADO) {
            throw new IllegalStateException("Não é possível cancelar um agendamento já realizado.");
        }
        this.status = StatusAgendamento.CANCELADO;
    }

    public void confirmar() {
        if (this.status == StatusAgendamento.CANCELADO) {
            throw new IllegalStateException("Não é possível confirmar um agendamento cancelado.");
        }
        this.status = StatusAgendamento.CONFIRMADO;
    }

    public void marcarComoRealizado() {
        if (this.status == StatusAgendamento.CANCELADO) {
            throw new IllegalStateException("Não é possível concluir um agendamento cancelado.");
        }
        this.status = StatusAgendamento.REALIZADO;
    }

    @Override
    @JsonIgnore
    public String getCodigoIdentificador() {
        return protocolo;
    }

    @Override
    @JsonIgnore
    public String formatarParaChat() {
        return "*" + medico.getEspecialidade().getNome() + "*\n"
                + "   Médico: " + medico.getNome() + "\n"
                + "   Data: " + dataHora.format(CHAT_DATE_FORMATTER) + "\n"
                + "   Status: " + status + "\n"
                + "   Protocolo: `" + protocolo + "`";
    }

    @Override
    @JsonIgnore
    public boolean estaAtivo() {
        return status == StatusAgendamento.AGENDADO || status == StatusAgendamento.CONFIRMADO;
    }
}
