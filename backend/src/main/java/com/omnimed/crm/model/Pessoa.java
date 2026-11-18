package com.omnimed.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Abstração de pessoa no domínio clínico (base para Paciente e Médico).
 * Aplica herança de atributos comuns, encapsulamento com validação de estado
 * e contratos polimórficos para identificação e apresentação de perfil.
 */
@MappedSuperclass
public abstract class Pessoa extends EntidadeBase {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 100)
    private String email;

    @Column(length = 25)
    private String telefone;

    protected Pessoa() {
    }

    protected Pessoa(String nome, String email, String telefone) {
        setNome(nome);
        setEmail(email);
        setTelefone(telefone);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome não pode ser nulo ou vazio.");
        }
        this.nome = nome.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = (email != null) ? email.trim() : null;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = (telefone != null) ? telefone.trim() : null;
    }

    /**
     * Retorna o documento ou registro profissional principal da pessoa (ex.: CPF ou CRM).
     */
    @JsonIgnore
    public abstract String getIdentificacaoPrincipal();

    /**
     * Retorna o papel/tipo da pessoa no sistema.
     */
    @JsonIgnore
    public abstract String getTipoPessoa();

    /**
     * Retorna um resumo formatado do perfil para exibição polimórfica.
     */
    @JsonIgnore
    public abstract String getResumoPerfil();
}
