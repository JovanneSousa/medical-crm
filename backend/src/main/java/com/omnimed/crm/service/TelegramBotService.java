package com.omnimed.crm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnimed.crm.channel.TelegramCanalComunicacao;
import com.omnimed.crm.model.CanalTipo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TelegramBotService {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);

    @Value("${omnimed.telegram.enabled:false}")
    private boolean enabled;

    @Value("${omnimed.telegram.bot-token:}")
    private String botToken;

    @Value("${omnimed.telegram.polling-interval-ms:2000}")
    private long pollingIntervalMs;

    private final ConversationalEngineService conversationalEngine;
    private final TelegramCanalComunicacao telegramCanal;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private long lastUpdateId = 0;

    public TelegramBotService(ConversationalEngineService conversationalEngine,
                              TelegramCanalComunicacao telegramCanal) {
        this.conversationalEngine = conversationalEngine;
        this.telegramCanal = telegramCanal;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        if (enabled && botToken != null && !botToken.isBlank()) {
            log.info("🤖 Iniciando serviço do Telegram Bot com Long Polling...");
            running.set(true);
            executorService.submit(this::pollUpdatesLoop);
        } else {
            log.info("ℹ️ Telegram Bot desativado no momento. Defina omnimed.telegram.enabled=true e o token para ativar polling automático.");
        }
    }

    @PreDestroy
    public void destroy() {
        running.set(false);
        executorService.shutdown();
    }

    private void pollUpdatesLoop() {
        while (running.get()) {
            try {
                pollUpdates();
                Thread.sleep(pollingIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Erro no polling do Telegram: {}", e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public void pollUpdates() {
        try {
            String url = String.format("https://api.telegram.org/bot%s/getUpdates?offset=%d&timeout=10",
                    botToken, lastUpdateId + 1);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.path("ok").asBoolean()) {
                    JsonNode result = root.path("result");
                    for (JsonNode update : result) {
                        long updateId = update.path("update_id").asLong();
                        if (updateId > lastUpdateId) {
                            lastUpdateId = updateId;
                        }
                        processarUpdate(update);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Polling update falhou: {}", e.getMessage());
        }
    }

    public void processarUpdate(JsonNode update) {
        try {
            JsonNode message = update.path("message");
            if (message.isMissingNode() || message.isNull()) {
                return;
            }

            String chatId = message.path("chat").path("id").asText();
            String text = message.path("text").asText("");

            if (chatId != null && !chatId.isBlank() && !text.isBlank()) {
                String resposta = conversationalEngine.processarMensagem(CanalTipo.TELEGRAM, chatId, text);
                if (resposta != null && !resposta.isBlank()) {
                    enviarMensagem(chatId, resposta);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao processar mensagem do Telegram: ", e);
        }
    }

    public boolean enviarMensagem(String chatId, String texto) {
        return telegramCanal.enviarMensagem(chatId, texto);
    }
}
