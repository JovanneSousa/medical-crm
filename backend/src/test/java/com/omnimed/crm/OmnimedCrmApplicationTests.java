package com.omnimed.crm;

import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.service.ConversationalEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OmnimedCrmApplicationTests {

    @Autowired
    private ConversationalEngineService conversationalEngine;

    @Test
    void contextLoads() {
        assertNotNull(conversationalEngine);
    }

    @Test
    void testFluxoBoasVindasEIdentificacao() {
        String respostaInicio = conversationalEngine.processarMensagem(CanalTipo.WEB_CHAT, "teste_session_1", "Olá");
        assertTrue(respostaInicio.contains("CPF"));

        // Enviar CPF de teste cadastrado (12345678900 - João da Silva)
        String respostaCpf = conversationalEngine.processarMensagem(CanalTipo.WEB_CHAT, "teste_session_1", "123.456.789-00");
        assertTrue(respostaCpf.contains("João da Silva"));
        assertTrue(respostaCpf.contains("Agendar Consulta Médica"));
    }

    @Test
    void testSafetyBoundaryEmergencia() {
        String resposta = conversationalEngine.processarMensagem(CanalTipo.WEB_CHAT, "teste_session_emergencia", "socorro estou tendo um infarto");
        assertTrue(resposta.contains("EMERGÊNCIA"));
        assertTrue(resposta.contains("192"));
    }
}
