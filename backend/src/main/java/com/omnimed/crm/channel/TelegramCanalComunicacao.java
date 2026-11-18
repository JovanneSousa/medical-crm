package com.omnimed.crm.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.model.Paciente;
import com.omnimed.crm.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Implementação concreta do canal Telegram via Bot API.
 */
@Component
public class TelegramCanalComunicacao extends AbstractCanalComunicacao {

    @Value("${omnimed.telegram.enabled:false}")
    private boolean enabled;

    @Value("${omnimed.telegram.bot-token:}")
    private String botToken;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TelegramCanalComunicacao() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public CanalTipo getTipoCanal() {
        return CanalTipo.TELEGRAM;
    }

    @Override
    public boolean isAtivo() {
        return enabled && botToken != null && !botToken.isBlank();
    }

    @Override
    protected boolean executarEnvio(String chatId, String texto) {
        try {
            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
            String payload = objectMapper.writeValueAsString(new SendMessagePayload(chatId, texto, "Markdown"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem para Telegram chatId {}: {}", chatId, e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Paciente> localizarPaciente(String identificadorBruto, PacienteRepository pacienteRepository) {
        return pacienteRepository.findByTelegramChatId(normalizarIdentificador(identificadorBruto));
    }

    @Override
    public boolean vincularPaciente(Paciente paciente, String identificadorBruto) {
        return paciente.vincularTelegramSeAusente(normalizarIdentificador(identificadorBruto));
    }

    @Override
    public Paciente criarPacienteComCanal(String nome, String cpf, String telefone, String email, String identificadorBruto) {
        return new Paciente(nome, cpf, telefone, email, normalizarIdentificador(identificadorBruto), null);
    }

    private record SendMessagePayload(String chat_id, String text, String parse_mode) {
    }
}
