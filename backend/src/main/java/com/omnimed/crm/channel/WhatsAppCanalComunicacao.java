package com.omnimed.crm.channel;

import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.model.Paciente;
import com.omnimed.crm.repository.PacienteRepository;
import com.omnimed.crm.service.WhatsAppService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementação concreta do canal WhatsApp integrada ao {@link WhatsAppService}.
 */
@Component
public class WhatsAppCanalComunicacao extends AbstractCanalComunicacao {

    private final WhatsAppService whatsAppService;

    public WhatsAppCanalComunicacao(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @Override
    public CanalTipo getTipoCanal() {
        return CanalTipo.WHATSAPP;
    }

    @Override
    public boolean isAtivo() {
        return whatsAppService.isEnabled();
    }

    @Override
    protected boolean executarEnvio(String numeroNormalizado, String texto) {
        return whatsAppService.enviarMensagem(numeroNormalizado, texto);
    }

    @Override
    public String normalizarIdentificador(String identificadorBruto) {
        if (identificadorBruto == null) {
            return "";
        }
        return identificadorBruto.replaceAll("@.*", "").replaceAll("[^0-9]", "");
    }

    @Override
    public Optional<Paciente> localizarPaciente(String identificadorBruto, PacienteRepository pacienteRepository) {
        String numeroLimpo = normalizarIdentificador(identificadorBruto);
        return pacienteRepository.findByWhatsappNumber(numeroLimpo)
                .or(() -> pacienteRepository.findByWhatsappNumber(identificadorBruto));
    }

    @Override
    public boolean vincularPaciente(Paciente paciente, String identificadorBruto) {
        return paciente.vincularWhatsappSeAusente(normalizarIdentificador(identificadorBruto));
    }

    @Override
    public Paciente criarPacienteComCanal(String nome, String cpf, String telefone, String email, String identificadorBruto) {
        return new Paciente(nome, cpf, telefone, email, null, normalizarIdentificador(identificadorBruto));
    }

    @Override
    public Optional<String> obterDicaTelefoneCadastro() {
        return Optional.of("\n_(Ou digite *sim* para confirmar o uso do número deste WhatsApp)_");
    }

    @Override
    public Optional<String> resolverTelefoneCadastro(String entradaUsuario, String identificadorBruto) {
        if (entradaUsuario != null && (entradaUsuario.equalsIgnoreCase("sim")
                || entradaUsuario.equalsIgnoreCase("ok")
                || entradaUsuario.equalsIgnoreCase("usar este"))) {
            return Optional.of(normalizarIdentificador(identificadorBruto));
        }
        return Optional.empty();
    }
}
