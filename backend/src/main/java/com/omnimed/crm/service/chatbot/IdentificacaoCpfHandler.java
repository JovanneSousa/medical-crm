package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.Paciente;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.repository.PacienteRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Handler responsável pelos estados iniciais de identificação do paciente via CPF.
 */
@Component
public class IdentificacaoCpfHandler extends AbstractEstadoConversaHandler {

    private final PacienteRepository pacienteRepository;

    public IdentificacaoCpfHandler(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.INICIO, EstadoConversa.AGUARDANDO_CPF);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        String cpfLimpo = texto.replaceAll("[^0-9]", "");
        if (cpfLimpo.length() != 11) {
            return "❌ O CPF informado parece inválido. Por favor, digite os *11 dígitos do seu CPF* para prosseguir:";
        }

        Optional<Paciente> pacienteOpt = pacienteRepository.findByCpf(cpfLimpo);
        if (pacienteOpt.isPresent()) {
            Paciente paciente = pacienteOpt.get();
            if (canal.vincularPaciente(paciente, sessao.getCanalUsuarioId())) {
                pacienteRepository.save(paciente);
            }
            sessao.identificarPaciente(paciente);
            return "✅ Identificação realizada com sucesso!\n\n" + gerarMenuPrincipal(paciente.getNome());
        }

        sessao.iniciarCadastroComCpf(cpfLimpo);
        String cpfFormatado = Paciente.formatarCpf(cpfLimpo);
        return "🔍 *Não localizamos um cadastro para o CPF informado (" + cpfFormatado + ").*\n\n"
                + "Para agendar consultas e consultar seus exames, vamos realizar seu cadastro agora mesmo. É super rápido!\n\n"
                + "👉 *Por favor, digite o seu Nome Completo:*";
    }
}
