package com.omnimed.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Representa um slot de horário na agenda de um médico.
 * Encapsula a reserva e liberação do horário, garantindo integridade de disponibilidade.
 */
@Entity
@Table(name = "horarios_atendimento")
public class HorarioAtendimento extends EntidadeBase {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(nullable = false)
    private Boolean disponivel = true;

    public HorarioAtendimento() {
    }

    public HorarioAtendimento(Medico medico, LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        setMedico(medico);
        setData(data);
        setHoraInicio(horaInicio);
        setHoraFim(horaFim);
        this.disponivel = true;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }

    public Boolean getDisponivel() {
        return disponivel;
    }

    public void setDisponivel(Boolean disponivel) {
        this.disponivel = disponivel != null ? disponivel : Boolean.TRUE;
    }

    @JsonIgnore
    public boolean estaDisponivel() {
        return Boolean.TRUE.equals(this.disponivel);
    }

    /**
     * Reserva este slot de horário, protegendo contra dupla marcação.
     */
    public void reservar() {
        if (!estaDisponivel()) {
            throw new IllegalStateException("O horário selecionado não está mais disponível.");
        }
        this.disponivel = false;
    }

    /**
     * Libera este slot de horário novamente para a agenda médica.
     */
    public void liberar() {
        this.disponivel = true;
    }

    @JsonIgnore
    public LocalDateTime getDataHoraInicio() {
        return LocalDateTime.of(data, horaInicio);
    }
}
