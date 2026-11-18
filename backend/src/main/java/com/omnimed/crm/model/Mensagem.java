package com.omnimed.crm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Representa uma mensagem trocada dentro de uma sessão de atendimento omnichannel.
 */
@Entity
@Table(name = "mensagens")
public class Mensagem extends EntidadeBase {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id")
    private SessaoAtendimento sessao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RemetenteTipo remetente;

    @Column(nullable = false, length = 4000)
    private String conteudo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private Boolean lida = false;

    public Mensagem() {
        this.dataHora = LocalDateTime.now();
    }

    public Mensagem(SessaoAtendimento sessao, RemetenteTipo remetente, String conteudo) {
        setSessao(sessao);
        setRemetente(remetente);
        setConteudo(conteudo);
        this.dataHora = LocalDateTime.now();
        this.lida = false;
    }

    public SessaoAtendimento getSessao() {
        return sessao;
    }

    public void setSessao(SessaoAtendimento sessao) {
        this.sessao = sessao;
    }

    public RemetenteTipo getRemetente() {
        return remetente;
    }

    public void setRemetente(RemetenteTipo remetente) {
        this.remetente = remetente;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo != null ? conteudo : "";
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida != null ? lida : Boolean.FALSE;
    }

    public void marcarComoLida() {
        this.lida = true;
    }
}
