package com.omnimed.crm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Representa uma especialidade médica oferecida pela clínica.
 */
@Entity
@Table(name = "especialidades")
public class Especialidade extends EntidadeBase {

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    public Especialidade() {
    }

    public Especialidade(String nome, String descricao) {
        setNome(nome);
        setDescricao(descricao);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da especialidade é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = (descricao != null) ? descricao.trim() : null;
    }
}
