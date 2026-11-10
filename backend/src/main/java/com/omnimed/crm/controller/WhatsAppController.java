package com.omnimed.crm.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.service.ConversationalEngineService;
import com.omnimed.crm.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook/whatsapp")
public class WhatsAppController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppController.class);

    private final ConversationalEngineService conversationalEngine;
    private final WhatsAppService whatsAppService;
    private final ObjectMapper objectMapper;

    public WhatsAppController(ConversationalEngineService conversationalEngine,
                              WhatsAppService whatsAppService) {
        this.conversationalEngine = conversationalEngine;
        this.whatsAppService = whatsAppService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Handshake de verificação de Webhook (compatível com Meta Cloud API e health check)
     */
    @GetMapping
    public ResponseEntity<?> verificarWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        if (challenge != null) {
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.ok(Map.of(
                "status", "ONLINE",
                "service", "OmniMed Care WhatsApp Integration",
                "enabled", whatsAppService.isEnabled(),
                "testNumber", whatsAppService.getTestNumber(),
                "gatewayUrlConfigured", whatsAppService.getGatewayUrl() != null && !whatsAppService.getGatewayUrl().isBlank()
        ));
    }

    /**
     * Consulta status do gateway Evolution API (online, conectado, estado da sessão)
     */
    @GetMapping("/status")
    public ResponseEntity<?> consultarStatusGateway() {
        return ResponseEntity.ok(whatsAppService.consultarStatusGateway());
    }

    /**
     * Cria a instância omnimed na Evolution API, configura o webhook e obtém o QR Code
     */
    @PostMapping("/conectar")
    public ResponseEntity<?> conectarGateway() {
        return ResponseEntity.ok(whatsAppService.inicializarEConfigurarInstancia());
    }

    /**
     * Recebimento de mensagens em formato JSON (Evolution API, Baileys, Z-API ou Webhook personalizado)
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> receberMensagemJson(@RequestBody JsonNode payload) {
        try {
            log.info("📩 [WHATSAPP WEBHOOK] Payload recebido: {}", payload);

            String remetente = null;
            String texto = null;

            // Formato 1: Evolution API / Baileys { "data": { "key": { "remoteJid": "..." }, "message": { "conversation": "..." } } }
            if (payload.has("data") && payload.path("data").has("key")) {
                JsonNode data = payload.path("data");
                remetente = data.path("key").path("remoteJid").asText();
                if (data.has("message")) {
                    JsonNode messageNode = data.path("message");
                    if (messageNode.has("conversation")) {
                        texto = messageNode.path("conversation").asText();
                    } else if (messageNode.has("extendedTextMessage")) {
                        texto = messageNode.path("extendedTextMessage").path("text").asText();
                    }
                }
            }
            // Formato 2: Simples { "from": "...", "message": "..." } ou { "number": "...", "text": "..." }
            else if (payload.has("from")) {
                remetente = payload.path("from").asText();
                texto = payload.has("message") ? payload.path("message").asText() : payload.path("text").asText();
            } else if (payload.has("number")) {
                remetente = payload.path("number").asText();
                texto = payload.has("text") ? payload.path("text").asText() : payload.path("message").asText();
            } else if (payload.has("phone")) {
                remetente = payload.path("phone").asText();
                texto = payload.path("message").asText();
            }

            if (remetente == null || remetente.isBlank() || texto == null || texto.isBlank()) {
                return ResponseEntity.ok(Map.of("status", "IGNORED", "reason", "Remetente ou mensagem vazia"));
            }

            // Normaliza o número de telefone (remove sufixos como @s.whatsapp.net ou prefixos)
            String numeroLimpo = remetente.replaceAll("@.*", "").replaceAll("[^0-9]", "");

            String respostaBot = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, numeroLimpo, texto);

            if (respostaBot != null && !respostaBot.isBlank()) {
                whatsAppService.enviarMensagem(numeroLimpo, respostaBot);
            }

            return ResponseEntity.ok(Map.of(
                    "status", "PROCESSED",
                    "from", numeroLimpo,
                    "reply", respostaBot != null ? respostaBot : ""
            ));
        } catch (Exception e) {
            log.error("Erro ao processar webhook WhatsApp: ", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Suporte para Webhook formulário URL-encoded (como Twilio Sandbox gratuito)
     */
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> receberMensagemForm(@RequestParam Map<String, String> formData) {
        try {
            log.info("📩 [WHATSAPP FORM WEBHOOK] Dados recebidos: {}", formData);

            String from = formData.getOrDefault("From", formData.get("from"));
            String body = formData.getOrDefault("Body", formData.get("body"));

            if (from == null || body == null) {
                return ResponseEntity.badRequest().body("Parâmetros 'From' e 'Body' são obrigatórios.");
            }

            String numeroLimpo = from.replace("whatsapp:", "").replaceAll("[^0-9]", "");
            String respostaBot = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, numeroLimpo, body);

            if (respostaBot != null && !respostaBot.isBlank()) {
                whatsAppService.enviarMensagem(numeroLimpo, respostaBot);
            }

            return ResponseEntity.ok(Map.of(
                    "status", "PROCESSED",
                    "from", numeroLimpo,
                    "reply", respostaBot != null ? respostaBot : ""
            ));
        } catch (Exception e) {
            log.error("Erro ao processar form webhook WhatsApp: ", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para teste e simulação direta de envio do WhatsApp
     */
    @PostMapping("/simular")
    public ResponseEntity<?> simularMensagemWhatsApp(@RequestBody Map<String, String> req) {
        String numero = req.getOrDefault("from", whatsAppService.getTestNumber());
        String mensagem = req.getOrDefault("message", "menu");

        String numeroLimpo = numero.replaceAll("[^0-9]", "");
        String resposta = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, numeroLimpo, mensagem);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "canal", "WHATSAPP",
                "usuarioId", numeroLimpo,
                "mensagemEnviada", mensagem,
                "resposta", resposta != null ? resposta : ""
        ));
    }
}
