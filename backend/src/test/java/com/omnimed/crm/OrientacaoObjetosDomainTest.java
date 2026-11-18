package com.omnimed.crm;

import com.omnimed.crm.channel.CanalComunicacao;
import com.omnimed.crm.channel.DespachanteCanalService;
import com.omnimed.crm.channel.TelegramCanalComunicacao;
import com.omnimed.crm.channel.WebChatCanalComunicacao;
import com.omnimed.crm.channel.WhatsAppCanalComunicacao;
import com.omnimed.crm.model.Agendamento;
import com.omnimed.crm.model.CanalTipo;
import com.omnimed.crm.model.Especialidade;
import com.omnimed.crm.model.EstadoConversa;
import com.omnimed.crm.model.Exame;
import com.omnimed.crm.model.HorarioAtendimento;
import com.omnimed.crm.model.Medico;
import com.omnimed.crm.model.Paciente;
import com.omnimed.crm.model.Pessoa;
import com.omnimed.crm.model.RegistroClinico;
import com.omnimed.crm.model.StatusAgendamento;
import com.omnimed.crm.model.StatusExame;
import com.omnimed.crm.service.WhatsAppService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários demonstrando e validando os quatro pilares da POO no domínio OmniMed:
 * Abstração, Encapsulamento, Herança e Polimorfismo.
 */
class OrientacaoObjetosDomainTest {

    @Test
    void deveAplicarHerancaEPolimorfismoEmPessoa() {
        Especialidade cardio = new Especialidade("Cardiologia", "Coração");
        Pessoa paciente = new Paciente("Ana Paula", "123.456.789-00", "(11) 99999-0000", "ana@email.com", null);
        Pessoa medico = new Medico("Dr. Carlos", "CRM/SP 145892", cardio, "carlos@omnimed.com", "(11) 98888-1111");

        List<Pessoa> pessoas = List.of(paciente, medico);

        assertEquals("Paciente", pessoas.get(0).getTipoPessoa());
        assertEquals("123.456.789-00", pessoas.get(0).getIdentificacaoPrincipal());
        assertTrue(pessoas.get(0).getResumoPerfil().contains("Ana Paula"));

        assertEquals("Médico", pessoas.get(1).getTipoPessoa());
        assertEquals("CRM/SP 145892", pessoas.get(1).getIdentificacaoPrincipal());
        assertTrue(pessoas.get(1).getResumoPerfil().contains("Cardiologia"));
    }

    @Test
    void deveAplicarHerancaEPolimorfismoEmRegistroClinico() {
        Especialidade dermato = new Especialidade("Dermatologia", "Pele");
        Paciente paciente = new Paciente("Bruno Lima", "98765432100", "11977776666", "bruno@email.com", null);
        Medico medico = new Medico("Dra. Beatriz", "CRM/SP 198744", dermato, "bia@omnimed.com", "11988887777");

        RegistroClinico agendamento = new Agendamento(
                "OM-10203", paciente, medico, LocalDateTime.of(2026, 12, 1, 10, 0), CanalTipo.WHATSAPP, "Retorno");
        RegistroClinico exame = new Exame(
                "EX-2026-01", paciente, "Hemograma", LocalDate.of(2026, 11, 10), StatusExame.DISPONIVEL,
                "https://omnimed.com.br/laudos/1.pdf", "Normal");

        List<RegistroClinico> registros = List.of(agendamento, exame);

        assertEquals("OM-10203", registros.get(0).getCodigoIdentificador());
        assertTrue(registros.get(0).formatarParaChat().contains("Dermatologia"));
        assertTrue(registros.get(0).estaAtivo());

        assertEquals("EX-2026-01", registros.get(1).getCodigoIdentificador());
        assertTrue(registros.get(1).formatarParaChat().contains("Hemograma"));
        assertTrue(registros.get(1).estaAtivo());
    }

    @Test
    void deveProtegerInvariantesComEncapsulamento() {
        assertThrows(IllegalArgumentException.class, () ->
                new Paciente("Sem CPF Válido", "123", "11999999999", "a@b.com", null));

        Especialidade esp = new Especialidade("Ortopedia", "Ossos");
        Medico medico = new Medico("Dr. Rodrigo", "CRM/SP 167332", esp, "r@omnimed.com", "11933334444");
        assertTrue(medico.isAtivo());
        medico.inativar();
        assertFalse(medico.isAtivo());

        HorarioAtendimento horario = new HorarioAtendimento(
                medico, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30));
        horario.reservar();
        assertFalse(horario.estaDisponivel());
        assertThrows(IllegalStateException.class, horario::reservar);

        horario.liberar();
        assertTrue(horario.estaDisponivel());

        Paciente paciente = new Paciente("Carlos Souza", "11122233344", "11911112222", "c@email.com", null);
        Agendamento agendamento = new Agendamento(
                "OM-55555", paciente, medico, LocalDateTime.now().plusDays(1), CanalTipo.WEB_CHAT, "Consulta");
        agendamento.cancelar();
        assertEquals(StatusAgendamento.CANCELADO, agendamento.getStatus());
        assertFalse(agendamento.estaAtivo());
        assertThrows(IllegalStateException.class, agendamento::cancelar);
    }

    @Test
    void deveResolverCanaisPolimorficamente() {
        DespachanteCanalService despachante = new DespachanteCanalService(List.of(
                new TelegramCanalComunicacao(),
                new WhatsAppCanalComunicacao(new WhatsAppService()),
                new WebChatCanalComunicacao()
        ));

        CanalComunicacao whatsapp = despachante.obterCanal(CanalTipo.WHATSAPP);
        CanalComunicacao telegram = despachante.obterCanal(CanalTipo.TELEGRAM);
        CanalComunicacao webchat = despachante.obterCanal(CanalTipo.WEB_CHAT);

        assertEquals("5511999998888", whatsapp.normalizarIdentificador("5511999998888@s.whatsapp.net"));
        assertTrue(whatsapp.obterDicaTelefoneCadastro().isPresent());
        assertEquals("5511999998888",
                whatsapp.resolverTelefoneCadastro("sim", "5511999998888@s.whatsapp.net").orElseThrow());

        assertTrue(telegram.obterDicaTelefoneCadastro().isEmpty());
        assertTrue(webchat.enviarMensagem("web_1", "Olá"));
    }
}
