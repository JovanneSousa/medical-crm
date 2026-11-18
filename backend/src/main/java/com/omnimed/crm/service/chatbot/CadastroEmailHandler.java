package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.Paciente;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.repository.PacienteRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Handler responsável pela finalização do cadastro de novo paciente.
 */
@Component
public class CadastroEmailHandler extends AbstractEstadoConversaHandler {

    private final PacienteRepository pacienteRepository;

    public CadastroEmailHandler(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public Set<EstadoConversa> getEstadosSuportados() {
        return Set.of(EstadoConversa.CADASTRO_EMAIL);
    }

    @Override
    public String processar(SessaoAtendimento sessao, String texto, CanalComunicacao canal) {
        String email = texto.equalsIgnoreCase("pular") ? "" : texto.trim();

        Paciente novoPaciente = canal.criarPacienteComCanal(
                sessao.getTempNome(),
                sessao.getTempCpf(),
                sessao.getTempTelefone(),
                email,
                sessao.getCanalUsuarioId()
        );

        novoPaciente = pacienteRepository.save(novoPaciente);
        sessao.concluirCadastro(novoPaciente);

        return "🎉 *CADASTRO REALIZADO COM SUCESSO!*\n\n"
                + "Seja muito bem-vindo(a) à OmniMed Care, *" + novoPaciente.getNome() + "*!\n\n"
                + gerarMenuPrincipal(novoPaciente.getNome());
    }
}
