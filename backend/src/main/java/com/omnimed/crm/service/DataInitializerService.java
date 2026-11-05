package com.omnimed.crm.service;

import com.omnimed.crm.model.*;
import com.omnimed.crm.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DataInitializerService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializerService.class);

    private final EspecialidadeRepository especialidadeRepository;
    private final MedicoRepository medicoRepository;
    private final HorarioAtendimentoRepository horarioRepository;
    private final PacienteRepository pacienteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ExameRepository exameRepository;
    private final SessaoAtendimentoRepository sessaoRepository;
    private final MensagemRepository mensagemRepository;

    public DataInitializerService(EspecialidadeRepository especialidadeRepository,
                                  MedicoRepository medicoRepository,
                                  HorarioAtendimentoRepository horarioRepository,
                                  PacienteRepository pacienteRepository,
                                  AgendamentoRepository agendamentoRepository,
                                  ExameRepository exameRepository,
                                  SessaoAtendimentoRepository sessaoRepository,
                                  MensagemRepository mensagemRepository) {
        this.especialidadeRepository = especialidadeRepository;
        this.medicoRepository = medicoRepository;
        this.horarioRepository = horarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.exameRepository = exameRepository;
        this.sessaoRepository = sessaoRepository;
        this.mensagemRepository = mensagemRepository;
    }

    @Override
    public void run(String... args) {
        if (especialidadeRepository.count() > 0) {
            return;
        }

        log.info("🌱 Inicializando dados demonstrativos da plataforma OmniMed Care...");

        // 1. Especialidades
        Especialidade cardio = especialidadeRepository.save(new Especialidade("Cardiologia", "Diagnóstico e tratamento de doenças do coração e circulação"));
        Especialidade dermato = especialidadeRepository.save(new Especialidade("Dermatologia", "Cuidados clínicos e estéticos da pele, cabelos e unhas"));
        Especialidade ortopedia = especialidadeRepository.save(new Especialidade("Ortopedia", "Tratamento de lesões musculares, articulares e ossos"));
        Especialidade clinicaGeral = especialidadeRepository.save(new Especialidade("Clínica Geral", "Atendimento preventivo e consultas de rotina"));
        Especialidade gineco = especialidadeRepository.save(new Especialidade("Ginecologia", "Saúde integral da mulher e acompanhamento preventivo"));

        // 2. Médicos
        Medico drCarlos = medicoRepository.save(new Medico("Dr. Carlos Eduardo Mendes", "CRM/SP 145892", cardio, "carlos.mendes@omnimed.com", "(11) 98111-2233"));
        Medico draBeatriz = medicoRepository.save(new Medico("Dra. Beatriz Silveira", "CRM/SP 198744", dermato, "beatriz.silveira@omnimed.com", "(11) 98222-3344"));
        Medico drRodrigo = medicoRepository.save(new Medico("Dr. Rodrigo Takahashi", "CRM/SP 167332", ortopedia, "rodrigo.takahashi@omnimed.com", "(11) 98333-4455"));
        Medico draFernanda = medicoRepository.save(new Medico("Dra. Fernanda Albuquerque", "CRM/SP 204511", clinicaGeral, "fernanda.a@omnimed.com", "(11) 98444-5566"));
        Medico draJuliana = medicoRepository.save(new Medico("Dra. Juliana Prado", "CRM/SP 178901", gineco, "juliana.prado@omnimed.com", "(11) 98555-6677"));

        // 3. Horários Disponíveis para os próximos 5 dias
        LocalDate hoje = LocalDate.now();
        List<Medico> medicos = List.of(drCarlos, draBeatriz, drRodrigo, draFernanda, draJuliana);

        for (Medico m : medicos) {
            for (int d = 0; d <= 4; d++) {
                LocalDate data = hoje.plusDays(d);
                horarioRepository.save(new HorarioAtendimento(m, data, LocalTime.of(9, 0), LocalTime.of(9, 30)));
                horarioRepository.save(new HorarioAtendimento(m, data, LocalTime.of(10, 30), LocalTime.of(11, 0)));
                horarioRepository.save(new HorarioAtendimento(m, data, LocalTime.of(14, 0), LocalTime.of(14, 30)));
                horarioRepository.save(new HorarioAtendimento(m, data, LocalTime.of(16, 0), LocalTime.of(16, 30)));
            }
        }

        // 4. Pacientes de Teste
        Paciente joao = pacienteRepository.save(new Paciente("João da Silva", "12345678900", "(11) 99887-6655", "joao.silva@email.com", "123456789"));
        Paciente maria = pacienteRepository.save(new Paciente("Maria Oliveira Santos", "98765432100", "(11) 97766-5544", "maria.santos@email.com", "987654321"));
        Paciente lucas = pacienteRepository.save(new Paciente("Lucas Ferreira", "11122233344", "(11) 96655-4433", "lucas.ferreira@email.com", null));

        // 5. Agendamentos existentes
        agendamentoRepository.save(new Agendamento("OM-77312", joao, drCarlos, LocalDateTime.now().plusDays(1).withHour(14).withMinute(0), CanalTipo.TELEGRAM, "Primeira consulta de rotina"));
        agendamentoRepository.save(new Agendamento("OM-88421", maria, draBeatriz, LocalDateTime.now().plusDays(2).withHour(10).withMinute(30), CanalTipo.WEB_CHAT, "Avaliação dermatológica"));

        // 6. Exames de teste
        exameRepository.save(new Exame("EX-2026-901", joao, "Ecocardiograma Transtorácico", LocalDate.now().minusDays(5), StatusExame.DISPONIVEL, "https://omnimed.com.br/laudos/ecocardio-joao.pdf", "Laudo liberado pelo Dr. Carlos"));
        exameRepository.save(new Exame("EX-2026-902", joao, "Hemograma Completo + Lipidograma", LocalDate.now().minusDays(3), StatusExame.DISPONIVEL, "https://omnimed.com.br/laudos/sangue-joao.pdf", "Resultados normais"));
        exameRepository.save(new Exame("EX-2026-944", maria, "Biópsia de Lesão Cutânea", LocalDate.now().minusDays(2), StatusExame.EM_ANALISE, null, "Material em análise histopatológica"));

        // 7. Sessão de atendimento simulada (na fila da recepção para teste do livechat)
        SessaoAtendimento sessaoTransbordo = new SessaoAtendimento(CanalTipo.TELEGRAM, "telegram_user_999");
        sessaoTransbordo.setPaciente(lucas);
        sessaoTransbordo.setStatus(StatusSessao.AGUARDANDO_HUMANO);
        sessaoTransbordo.setEstadoConversa(EstadoConversa.ATENDIMENTO_HUMANO);
        sessaoTransbordo = sessaoRepository.save(sessaoTransbordo);

        mensagemRepository.save(new Mensagem(sessaoTransbordo, RemetenteTipo.PACIENTE, "Olá, boa tarde! Gostaria de saber se vocês aceitam a guia emitida pelo convênio Amil para cirurgia ambulatorial?"));
        mensagemRepository.save(new Mensagem(sessaoTransbordo, RemetenteTipo.BOT, "Transferindo para a recepção humana. Um atendente responderá em instantes."));

        log.info("✅ Dados de demonstração carregados com sucesso!");
    }
}
