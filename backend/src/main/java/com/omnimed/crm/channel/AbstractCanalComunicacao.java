package com.omnimed.crm.channel;

import com.omnimed.crm.model.Paciente;
import com.omnimed.crm.repository.PacienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Classe abstrata base para implementações de canais omnichannel.
 * Aplica o padrão Template Method em {@link #enviarMensagem(String, String)},
 * centralizando validações e delegando o transporte concreto às subclasses.
 */
public abstract class AbstractCanalComunicacao implements CanalComunicacao {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public final boolean enviarMensagem(String destinatarioId, String texto) {
        if (destinatarioId == null || destinatarioId.isBlank() || texto == null || texto.isBlank()) {
            log.debug("Envio ignorado no canal {}: destinatário ou texto vazio.", getTipoCanal());
            return false;
        }
        if (!isAtivo()) {
            log.info("Canal {} desativado. Mensagem não enviada para {}.", getTipoCanal(), destinatarioId);
            return false;
        }
        String destinatarioNormalizado = normalizarIdentificador(destinatarioId);
        return executarEnvio(destinatarioNormalizado, texto);
    }

    /**
     * Passo concreto implementado por cada subclasse para despachar a mensagem no canal específico.
     */
    protected abstract boolean executarEnvio(String destinatarioNormalizado, String texto);

    @Override
    public String normalizarIdentificador(String identificadorBruto) {
        return identificadorBruto != null ? identificadorBruto.trim() : "";
    }

    @Override
    public Optional<Paciente> localizarPaciente(String identificadorBruto, PacienteRepository pacienteRepository) {
        return Optional.empty();
    }

    @Override
    public boolean vincularPaciente(Paciente paciente, String identificadorBruto) {
        return false;
    }

    @Override
    public Paciente criarPacienteComCanal(String nome, String cpf, String telefone, String email, String identificadorBruto) {
        return new Paciente(nome, cpf, telefone, email, null, null);
    }
}
