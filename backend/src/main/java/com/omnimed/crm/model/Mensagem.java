package com.omnimed.crm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensagens")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
        this.sessao = sessao;
        this.remetente = remetente;
        this.conteudo = conteudo;
        this.dataHora = LocalDateTime.now();
        this.lida = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        this.conteudo = conteudo;
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
        this.lida = lida;
    }
}
