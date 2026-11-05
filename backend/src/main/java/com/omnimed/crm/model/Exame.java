package com.omnimed.crm.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "exames")
public class Exame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_exame", nullable = false, unique = true, length = 30)
    private String codigoExame;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(name = "tipo_exame", nullable = false, length = 150)
    private String tipoExame;

    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusExame status;

    @Column(name = "laudo_url", length = 300)
    private String laudoUrl;

    @Column(length = 500)
    private String observacoes;

    public Exame() {}

    public Exame(String codigoExame, Paciente paciente, String tipoExame, LocalDate dataColeta, StatusExame status, String laudoUrl, String observacoes) {
        this.codigoExame = codigoExame;
        this.paciente = paciente;
        this.tipoExame = tipoExame;
        this.dataColeta = dataColeta;
        this.status = status;
        this.laudoUrl = laudoUrl;
        this.observacoes = observacoes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoExame() {
        return codigoExame;
    }

    public void setCodigoExame(String codigoExame) {
        this.codigoExame = codigoExame;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public String getTipoExame() {
        return tipoExame;
    }

    public void setTipoExame(String tipoExame) {
        this.tipoExame = tipoExame;
    }

    public LocalDate getDataColeta() {
        return dataColeta;
    }

    public void setDataColeta(LocalDate dataColeta) {
        this.dataColeta = dataColeta;
    }

    public StatusExame getStatus() {
        return status;
    }

    public void setStatus(StatusExame status) {
        this.status = status;
    }

    public String getLaudoUrl() {
        return laudoUrl;
    }

    public void setLaudoUrl(String laudoUrl) {
        this.laudoUrl = laudoUrl;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
