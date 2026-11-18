package com.omnimed.crm.service.chatbot;

import com.omnimed.crm.model.RegistroClinico;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Classe abstrata base para os handlers de estado da conversa.
 * Compartilha utilitários encapsulados de formatação e parsing de opções,
 * evitando duplicação entre as subclasses concretas.
 */
public abstract class AbstractEstadoConversaHandler implements EstadoConversaHandler {

    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    protected String gerarMenuPrincipal(String nomePaciente) {
        return "Olá, *" + nomePaciente + "*! Como podemos ajudar hoje?\n\n"
                + "1️⃣ *Agendar Consulta Médica*\n"
                + "2️⃣ *Minhas Consultas Agendadas*\n"
                + "3️⃣ *Consultar Meus Exames / Laudos*\n"
                + "4️⃣ *Informações e Convênios*\n"
                + "5️⃣ *Falar com um Atendente Humano*\n"
                + "6️⃣ *Encerrar Atendimento*\n\n"
                + "👉 _Digite o número da opção desejada:_";
    }

    protected String gerarMensagemDespedida() {
        return "👋 *Atendimento Finalizado com Sucesso!*\n\n"
                + "Muito obrigado por entrar em contato com a *OmniMed Care*. Cuidar da sua saúde é a nossa prioridade!\n\n"
                + "Caso precise de novo atendimento a qualquer momento, basta enviar uma mensagem ou digitar *menu*.\n\n"
                + "Tenha um excelente dia! 🩺✨";
    }

    protected Optional<Integer> parsearIndiceEscolhido(String texto, int tamanhoMaximo) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        try {
            int indice = Integer.parseInt(texto.trim()) - 1;
            if (indice >= 0 && indice < tamanhoMaximo) {
                return Optional.of(indice);
            }
        } catch (NumberFormatException ignored) {
        }
        return Optional.empty();
    }

    /**
     * Formata uma lista heterogênea ou homogênea de {@link RegistroClinico}
     * usando despacho polimórfico em {@link RegistroClinico#formatarParaChat()}.
     */
    protected String formatarRegistrosClinicos(List<? extends RegistroClinico> registros, boolean numerado) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < registros.size(); i++) {
            RegistroClinico registro = registros.get(i);
            if (numerado) {
                sb.append(i + 1).append(". ");
            }
            sb.append(registro.formatarParaChat()).append("\n");
            if (numerado) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
