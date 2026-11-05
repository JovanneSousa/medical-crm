package com.omnimed.crm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessoes_atendimento")
public class SessaoAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CanalTipo canal;

    @Column(name = "canal_usuario_id", nullable = false, length = 100)
    private String canalUsuarioId; // Ex: Telegram ChatId ou UUID de WebChat

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusSessao status;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conversa", nullable = false, length = 40)
    private EstadoConversa estadoConversa;

    // Metadados temporários da sessão para orquestração guiada
    @Column(name = "temp_especialidade_id")
    private Long tempEspecialidadeId;

    @Column(name = "temp_medico_id")
    private Long tempMedicoId;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_ultima_interacao", nullable = false)
    private LocalDateTime dataUltimaInteracao;

    public SessaoAtendimento() {
        this.dataInicio = LocalDateTime.now();
        this.dataUltimaInteracao = LocalDateTime.now();
        this.status = StatusSessao.BOT;
        this.estadoConversa = EstadoConversa.INICIO;
    }

    public SessaoAtendimento(CanalTipo canal, String canalUsuarioId) {
        this.canal = canal;
        this.canalUsuarioId = canalUsuarioId;
        this.dataInicio = LocalDateTime.now();
        this.dataUltimaInteracao = LocalDateTime.now();
        this.status = StatusSessao.BOT;
        this.estadoConversa = EstadoConversa.INICIO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CanalTipo getCanal() {
        return canal;
    }

    public void setCanal(CanalTipo canal) {
        this.canal = canal;
    }

    public String getCanalUsuarioId() {
        return canalUsuarioId;
    }

    public void setCanalUsuarioId(String canalUsuarioId) {
        this.canalUsuarioId = canalUsuarioId;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public StatusSessao getStatus() {
        return status;
    }

    public void setStatus(StatusSessao status) {
        this.status = status;
    }

    public EstadoConversa getEstadoConversa() {
        return estadoConversa;
    }

    public void setEstadoConversa(EstadoConversa estadoConversa) {
        this.estadoConversa = estadoConversa;
    }

    public Long getTempEspecialidadeId() {
        return tempEspecialidadeId;
    }

    public void setTempEspecialidadeId(Long tempEspecialidadeId) {
        this.tempEspecialidadeId = tempEspecialidadeId;
    }

    public Long getTempMedicoId() {
        return tempMedicoId;
    }

    public void setTempMedicoId(Long tempMedicoId) {
        this.tempMedicoId = tempMedicoId;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataUltimaInteracao() {
        return dataUltimaInteracao;
    }

    public void setDataUltimaInteracao(LocalDateTime dataUltimaInteracao) {
        this.dataUltimaInteracao = dataUltimaInteracao;
    }
}
