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

    @Test
    void testFluxoCadastroEAgendamentoPolimorficoViaWhatsapp() {
        String usuarioId = "5511988887777";
        String r1 = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, usuarioId, "55566677788");
        assertTrue(r1.contains("Nome Completo"));

        String r2 = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, usuarioId, "Mariana Costa");
        assertTrue(r2.contains("WhatsApp"));

        String r3 = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, usuarioId, "sim");
        assertTrue(r3.contains("e-mail"));

        String r4 = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, usuarioId, "pular");
        assertTrue(r4.contains("CADASTRO REALIZADO COM SUCESSO"));
        assertTrue(r4.contains("Mariana Costa"));

        // Escolher Agendar Consulta (1) -> Especialidade (1) -> Médico (1) -> Horário (1)
        String rEsp = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, usuarioId, "1");
        assertTrue(rEsp.contains("Especialidade"));

        String rMed = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, usuarioId, "1");
        assertTrue(rMed.contains("Médicos Disponíveis"));

        String rHor = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, usuarioId, "1");
        assertTrue(rHor.contains("Horários Livres"));

        String rConf = conversationalEngine.processarMensagem(CanalTipo.WHATSAPP, usuarioId, "1");
        assertTrue(rConf.contains("CONSULTA AGENDADA COM SUCESSO"));
        assertTrue(rConf.contains("OM-"));
    }
}
