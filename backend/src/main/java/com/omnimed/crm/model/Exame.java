package com.omnimed.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Representa um exame laboratorial ou de imagem vinculado a um paciente.
 * Herda de {@link RegistroClinico} e implementa formatação polimórfica para chat.
 */
@Entity
@Table(name = "exames")
public class Exame extends RegistroClinico {

    @Column(name = "codigo_exame", nullable = false, unique = true, length = 30)
    private String codigoExame;

    @Column(name = "tipo_exame", nullable = false, length = 150)
    private String tipoExame;

    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusExame status;

    @Column(name = "laudo_url", length = 300)
    private String laudoUrl;

    public Exame() {
    }

    public Exame(String codigoExame, Paciente paciente, String tipoExame, LocalDate dataColeta,
                 StatusExame status, String laudoUrl, String observacoes) {
        super(paciente, observacoes);
        setCodigoExame(codigoExame);
        setTipoExame(tipoExame);
        setDataColeta(dataColeta);
        setStatus(status);
        setLaudoUrl(laudoUrl);
    }

    public String getCodigoExame() {
        return codigoExame;
    }

    public void setCodigoExame(String codigoExame) {
        if (codigoExame == null || codigoExame.isBlank()) {
            throw new IllegalArgumentException("O código do exame é obrigatório.");
        }
        this.codigoExame = codigoExame.trim();
    }

    public String getTipoExame() {
        return tipoExame;
    }

    public void setTipoExame(String tipoExame) {
        if (tipoExame == null || tipoExame.isBlank()) {
            throw new IllegalArgumentException("O tipo do exame é obrigatório.");
        }
        this.tipoExame = tipoExame.trim();
    }

    public LocalDate getDataColeta() {
        return dataColeta;
    }

    public void setDataColeta(LocalDate dataColeta) {
        if (dataColeta == null) {
            throw new IllegalArgumentException("A data de coleta do exame é obrigatória.");
        }
        this.dataColeta = dataColeta;
    }

    public StatusExame getStatus() {
        return status;
    }

    public void setStatus(StatusExame status) {
        this.status = status != null ? status : StatusExame.EM_ANALISE;
    }

    public String getLaudoUrl() {
        return laudoUrl;
    }

    public void setLaudoUrl(String laudoUrl) {
        this.laudoUrl = (laudoUrl != null && !laudoUrl.isBlank()) ? laudoUrl.trim() : null;
    }

    public void disponibilizarLaudo(String url) {
        setLaudoUrl(url);
        this.status = StatusExame.DISPONIVEL;
    }

    public void marcarComoRetirado() {
        this.status = StatusExame.RETIRADO;
    }

    @Override
    @JsonIgnore
    public String getCodigoIdentificador() {
        return codigoExame;
    }

    @Override
    @JsonIgnore
    public String formatarParaChat() {
        StringBuilder sb = new StringBuilder();
        sb.append("• *").append(tipoExame).append("*\n")
          .append("  Código: ").append(codigoExame).append("\n")
          .append("  Data da Coleta: ").append(dataColeta).append("\n")
          .append("  Status: *").append(status).append("*\n");
        if (laudoUrl != null && !laudoUrl.isBlank()) {
            sb.append("  📄 Laudo online: ").append(laudoUrl).append("\n");
        }
        return sb.toString();
    }

    @Override
    @JsonIgnore
    public boolean estaAtivo() {
        return status != StatusExame.RETIRADO;
    }
}
