package com.omnimed.crm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${omnimed.whatsapp.enabled:true}")
    private boolean enabled;

    @Value("${omnimed.whatsapp.gateway-base-url:http://localhost:8081}")
    private String gatewayBaseUrl;

    @Value("${omnimed.whatsapp.gateway-url:http://localhost:8081/message/sendText/omnimed}")
    private String gatewayUrl;

    @Value("${omnimed.whatsapp.gateway-api-key:omnimed_secret_token_123}")
    private String gatewayApiKey;

    @Value("${omnimed.whatsapp.instance-name:omnimed}")
    private String instanceName;

    @Value("${omnimed.whatsapp.test-number:5511999998888}")
    private String testNumber;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WhatsAppService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTestNumber() {
        return testNumber;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public String getGatewayBaseUrl() {
        return gatewayBaseUrl;
    }

    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Envia mensagem de texto para o número WhatsApp especificado.
     */
    public boolean enviarMensagem(String destinatario, String texto) {
        if (!enabled) {
            log.info("WhatsApp desativado. Mensagem não enviada para {}: {}", destinatario, texto);
            return false;
        }

        String numeroLimpo = destinatario.replaceAll("[^0-9]", "");
        log.info("📱 [WHATSAPP OUTBOUND] Para: {} | Mensagem: {}", numeroLimpo, texto);

        if (gatewayUrl != null && !gatewayUrl.isBlank()) {
            try {
                Map<String, Object> payload = Map.of(
                        "number", numeroLimpo,
                        "text", texto
                );
                String jsonBody = objectMapper.writeValueAsString(payload);

                HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(gatewayUrl))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

                if (gatewayApiKey != null && !gatewayApiKey.isBlank()) {
                    reqBuilder.header("apikey", gatewayApiKey);
                }

                HttpResponse<String> response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
                log.info("Resposta Gateway WhatsApp (status {}): {}", response.statusCode(), response.body());
                return response.statusCode() >= 200 && response.statusCode() < 300;
            } catch (Exception e) {
                log.warn("Gateway WhatsApp externo inacessível ou offline: {}", e.getMessage());
                return false;
            }
        }

        return true;
    }

    /**
     * Consulta o estado atual da conexão com o WhatsApp na Evolution API.
     */
    public Map<String, Object> consultarStatusGateway() {
        Map<String, Object> status = new HashMap<>();
        status.put("gatewayBaseUrl", gatewayBaseUrl);
        status.put("instanceName", instanceName);
        status.put("configured", true);

        try {
            String url = String.format("%s/instance/connectionState/%s", gatewayBaseUrl, instanceName);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(4))
                    .header("apikey", gatewayApiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                String state = json.path("instance").path("state").asText("desconhecido");
                status.put("online", true);
                status.put("connectionState", state);
                status.put("conectado", "open".equalsIgnoreCase(state));
            } else {
                status.put("online", true);
                status.put("connectionState", "instancia_nao_iniciada");
                status.put("conectado", false);
            }
        } catch (Exception e) {
            status.put("online", false);
            status.put("connectionState", "gateway_offline");
            status.put("conectado", false);
            status.put("motivo", "Não foi possível conectar ao container Evolution API na porta 8081.");
        }

        return status;
    }

    /**
     * Inicializa a instância na Evolution API e configura automaticamente o Webhook para o OmniMed.
     */
    public Map<String, Object> inicializarEConfigurarInstancia() {
        Map<String, Object> resultado = new HashMap<>();

        try {
            // 1. Tentar criar a instância caso não exista
            String createUrl = String.format("%s/instance/create", gatewayBaseUrl);
            Map<String, Object> createPayload = Map.of(
                    "instanceName", instanceName,
                    "token", gatewayApiKey,
                    "qrcode", true,
                    "integration", "WHATSAPP-BAILEYS"
            );

            HttpRequest createReq = HttpRequest.newBuilder()
                    .uri(URI.create(createUrl))
                    .timeout(Duration.ofSeconds(6))
                    .header("Content-Type", "application/json")
                    .header("apikey", gatewayApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(createPayload)))
                    .build();

            httpClient.send(createReq, HttpResponse.BodyHandlers.ofString());

            // 2. Configurar o Webhook apontando para o OmniMed (host.docker.internal para alcançar o Spring Boot)
            String webhookUrl = String.format("%s/webhook/set/%s", gatewayBaseUrl, instanceName);
            Map<String, Object> webhookPayload = Map.of(
                    "enabled", true,
                    "url", "http://host.docker.internal:8080/api/webhook/whatsapp",
                    "webhook_by_events", false,
                    "events", List.of("MESSAGES_UPSERT")
            );

            HttpRequest webhookReq = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(6))
                    .header("Content-Type", "application/json")
                    .header("apikey", gatewayApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(webhookPayload)))
                    .build();

            httpClient.send(webhookReq, HttpResponse.BodyHandlers.ofString());

            // 3. Buscar o QR Code atual para conexão
            String connectUrl = String.format("%s/instance/connect/%s", gatewayBaseUrl, instanceName);
            HttpRequest connectReq = HttpRequest.newBuilder()
                    .uri(URI.create(connectUrl))
                    .timeout(Duration.ofSeconds(6))
                    .header("apikey", gatewayApiKey)
                    .GET()
                    .build();

            HttpResponse<String> connectRes = httpClient.send(connectReq, HttpResponse.BodyHandlers.ofString());
            if (connectRes.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(connectRes.body());
                resultado.put("sucesso", true);
                resultado.put("base64", json.path("base64").asText(""));
                resultado.put("code", json.path("code").asText(""));
                resultado.put("pairingCode", json.path("pairingCode").asText(""));
            } else {
                resultado.put("sucesso", true);
                resultado.put("mensagem", "Instância pronta. Aguardando conexão.");
            }
        } catch (Exception e) {
            resultado.put("sucesso", false);
            resultado.put("erro", e.getMessage());
        }

        return resultado;
    }
}
