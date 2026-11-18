package com.omnimed.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Representa um paciente cadastrado na plataforma OmniMed.
 * Herda atributos comuns e contratos polimórficos de {@link Pessoa}.
 */
@Entity
@Table(name = "pacientes")
public class Paciente extends Pessoa {

    @Column(unique = true, length = 14)
    private String cpf;

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
        this(nome, cpf, telefone, email, telegramChatId, null);
    }

    public Paciente(String nome, String cpf, String telefone, String email, String telegramChatId, String whatsappNumber) {
        super(nome, email, telefone);
        setCpf(cpf);
        setTelegramChatId(telegramChatId);
        setWhatsappNumber(whatsappNumber);
        this.dataCadastro = LocalDateTime.now();
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = normalizarEValidarCpf(cpf);
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = (telegramChatId != null && !telegramChatId.isBlank()) ? telegramChatId.trim() : null;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        if (whatsappNumber == null || whatsappNumber.isBlank()) {
            this.whatsappNumber = null;
            return;
        }
        String digitos = whatsappNumber.replaceAll("[^0-9]", "");
        this.whatsappNumber = digitos.isBlank() ? null : digitos;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro != null ? dataCadastro : LocalDateTime.now();
    }

    /**
     * Encapsula o vínculo do identificador do Telegram apenas se ainda não houver um associado.
     */
    public boolean vincularTelegramSeAusente(String chatId) {
        if ((this.telegramChatId == null || this.telegramChatId.isBlank()) && chatId != null && !chatId.isBlank()) {
            setTelegramChatId(chatId);
            return true;
        }
        return false;
    }

    /**
     * Encapsula o vínculo do número WhatsApp apenas se ainda não houver um associado.
     */
    public boolean vincularWhatsappSeAusente(String numero) {
        if ((this.whatsappNumber == null || this.whatsappNumber.isBlank()) && numero != null && !numero.isBlank()) {
            setWhatsappNumber(numero);
            return true;
        }
        return false;
    }

    @JsonIgnore
    public String getCpfFormatado() {
        return formatarCpf(this.cpf);
    }

    public static String formatarCpf(String cpf) {
        if (cpf == null) {
            return "";
        }
        String digitos = cpf.replaceAll("[^0-9]", "");
        if (digitos.length() != 11) {
            return cpf;
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9, 11);
    }

    private String normalizarEValidarCpf(String cpfBruto) {
        if (cpfBruto == null || cpfBruto.isBlank()) {
            return null;
        }
        String digitos = cpfBruto.replaceAll("[^0-9]", "");
        if (digitos.length() != 11) {
            throw new IllegalArgumentException("CPF inválido: deve conter exatamente 11 dígitos.");
        }
        return digitos;
    }

    @Override
    @JsonIgnore
    public String getIdentificacaoPrincipal() {
        return getCpfFormatado();
    }

    @Override
    @JsonIgnore
    public String getTipoPessoa() {
        return "Paciente";
    }

    @Override
    @JsonIgnore
    public String getResumoPerfil() {
        return String.format("%s: %s (CPF: %s)", getTipoPessoa(), getNome(), getIdentificacaoPrincipal());
    }
}
