package com.omnimed.crm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(unique = true, length = 14)
    private String cpf;

    @Column(length = 25)
    private String telefone;

    @Column(length = 100)
    private String email;

    @Column(name = "telegram_chat_id", unique = true, length = 50)
    private String telegramChatId;

    @Column(name = "whatsapp_number", length = 50)
    private String whatsappNumber;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    public Paciente() {
        this.dataCadastro = LocalDateTime.now();
    }

    public Paciente(String nome, String cpf, String telefone, String email, String telegramChatId) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.telegramChatId = telegramChatId;
        this.dataCadastro = LocalDateTime.now();
    }

    public Paciente(String nome, String cpf, String telefone, String email, String telegramChatId, String whatsappNumber) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.telegramChatId = telegramChatId;
        this.whatsappNumber = whatsappNumber;
        this.dataCadastro = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
