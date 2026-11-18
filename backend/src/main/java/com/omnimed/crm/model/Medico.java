package com.omnimed.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Representa um profissional médico do corpo clínico da OmniMed.
 * Herda atributos comuns e contratos polimórficos de {@link Pessoa}.
 */
@Entity
@Table(name = "medicos")
public class Medico extends Pessoa {

    @Column(nullable = false, unique = true, length = 30)
    private String crm;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "especialidade_id")
    private Especialidade especialidade;

    @Column(nullable = false)
    private Boolean ativo = true;

    public Medico() {
    }

    public Medico(String nome, String crm, Especialidade especialidade, String email, String telefone) {
        super(nome, email, telefone);
        setCrm(crm);
        setEspecialidade(especialidade);
        this.ativo = true;
    }

    public String getCrm() {
        return crm;
    }

    public void setCrm(String crm) {
        if (crm == null || crm.isBlank()) {
            throw new IllegalArgumentException("O CRM do médico é obrigatório.");
        }
        this.crm = crm.trim();
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        if (especialidade == null) {
            throw new IllegalArgumentException("A especialidade do médico é obrigatória.");
        }
        this.especialidade = especialidade;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo != null ? ativo : Boolean.TRUE;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void inativar() {
        this.ativo = false;
    }

    @JsonIgnore
    public boolean isAtivo() {
        return Boolean.TRUE.equals(this.ativo);
    }

    @Override
    @JsonIgnore
    public String getIdentificacaoPrincipal() {
        return crm;
    }

    @Override
    @JsonIgnore
    public String getTipoPessoa() {
        return "Médico";
    }

    @Override
    @JsonIgnore
    public String getResumoPerfil() {
        String nomeEsp = especialidade != null ? especialidade.getNome() : "Sem especialidade";
        return String.format("%s (%s) - %s [%s]", getNome(), crm, nomeEsp, isAtivo() ? "Ativo" : "Inativo");
    }
}
