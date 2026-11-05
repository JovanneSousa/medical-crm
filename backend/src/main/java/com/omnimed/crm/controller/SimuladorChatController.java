package com.omnimed.crm.controller;

import com.omnimed.crm.dto.NovaMensagemDTO;
import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.service.ConversationalEngineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulador")
public class SimuladorChatController {

    private final ConversationalEngineService conversationalEngine;

    public SimuladorChatController(ConversationalEngineService conversationalEngine) {
        this.conversationalEngine = conversationalEngine;
    }

    @PostMapping("/mensagem")
    public ResponseEntity<Map<String, String>> enviarMensagemSimulada(
            @RequestParam(defaultValue = "WEB_CHAT") CanalTipo canal,
            @RequestParam(defaultValue = "simulador_user_1") String usuarioId,
            @Valid @RequestBody NovaMensagemDTO dto) {

        String resposta = conversationalEngine.processarMensagem(canal, usuarioId, dto.conteudo());
        return ResponseEntity.ok(Map.of(
                "resposta", (resposta != null) ? resposta : "Mensagem recebida pela recepção.",
                "canal", canal.name(),
                "usuarioId", usuarioId
        ));
    }
}
