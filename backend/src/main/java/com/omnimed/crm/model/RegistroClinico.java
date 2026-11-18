package com.omnimed.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

/**
 * Abstração para registros clínicos vinculados a um paciente (Consultas/Agendamentos e Exames).
 * Define comportamento polimórfico para exibição em canais conversacionais e controle de estado.
 */
@MappedSuperclass
public abstract class RegistroClinico extends EntidadeBase {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(length = 500)
    private String observacoes;

    protected RegistroClinico() {
    }

    protected RegistroClinico(Paciente paciente, String observacoes) {
        setPaciente(paciente);
        setObservacoes(observacoes);
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("O registro clínico deve estar associado a um paciente.");
        }
        this.paciente = paciente;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = (observacoes != null) ? observacoes.trim() : null;
    }

    /**
     * Código identificador único do registro clínico (protocolo de agendamento ou código de exame).
     */
    @JsonIgnore
    public abstract String getCodigoIdentificador();

    /**
     * Formatação polimórfica do registro clínico para canais de atendimento (WhatsApp, Telegram, WebChat).
     */
    @JsonIgnore
    public abstract String formatarParaChat();

    /**
     * Indica se o registro clínico ainda está em estado ativo no fluxo assistencial.
     */
    @JsonIgnore
    public abstract boolean estaAtivo();
}
