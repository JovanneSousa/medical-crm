package com.omnimed.crm.channel;

import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.model.Paciente;
import com.omnimed.crm.repository.PacienteRepository;

import java.util.Optional;

/**
 * Abstração que define o contrato polimórfico para os canais de comunicação omnichannel
 * (Telegram, WhatsApp, WebChat, etc.).
 */
public interface CanalComunicacao {

    CanalTipo getTipoCanal();

    boolean isAtivo();

    boolean enviarMensagem(String destinatarioId, String texto);

    String normalizarIdentificador(String identificadorBruto);

    Optional<Paciente> localizarPaciente(String identificadorBruto, PacienteRepository pacienteRepository);

    boolean vincularPaciente(Paciente paciente, String identificadorBruto);

    Paciente criarPacienteComCanal(String nome, String cpf, String telefone, String email, String identificadorBruto);

    default Optional<String> obterDicaTelefoneCadastro() {
        return Optional.empty();
    }

    default Optional<String> resolverTelefoneCadastro(String entradaUsuario, String identificadorBruto) {
        return Optional.empty();
    }
}
