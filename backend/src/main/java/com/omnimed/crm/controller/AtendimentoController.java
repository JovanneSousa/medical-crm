package com.omnimed.crm.controller;

import com.omnimed.crm.dto.MensagemDTO;
import com.omnimed.crm.dto.NovaMensagemDTO;
import com.omnimed.crm.dto.SessaoDTO;
import com.omnimed.crm.model.*;
import com.omnimed.crm.repository.MensagemRepository;
import com.omnimed.crm.repository.SessaoAtendimentoRepository;
import com.omnimed.crm.service.TelegramBotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/atendimentos")
public class AtendimentoController {

    private final SessaoAtendimentoRepository sessaoRepository;
    private final MensagemRepository mensagemRepository;
    private final TelegramBotService telegramBotService;

    public AtendimentoController(SessaoAtendimentoRepository sessaoRepository,
                                 MensagemRepository mensagemRepository,
                                 TelegramBotService telegramBotService) {
        this.sessaoRepository = sessaoRepository;
        this.mensagemRepository = mensagemRepository;
        this.telegramBotService = telegramBotService;
    }

    @GetMapping("/sessoes")
    public ResponseEntity<List<SessaoDTO>> listarSessoes(@RequestParam(required = false) StatusSessao status) {
        List<SessaoAtendimento> sessoes;
        if (status != null) {
            sessoes = sessaoRepository.findByStatusOrderByDataUltimaInteracaoDesc(status);
        } else {
            sessoes = sessaoRepository.findAll();
        }

        List<SessaoDTO> dtos = sessoes.stream().map(s -> {
            List<Mensagem> msgs = mensagemRepository.findTop50BySessaoIdOrderByDataHoraDesc(s.getId());
            String ultimaMsg = msgs.isEmpty() ? "" : msgs.get(0).getConteudo();
            return new SessaoDTO(
                    s.getId(),
                    s.getCanal(),
                    s.getCanalUsuarioId(),
                    s.getPaciente() != null ? s.getPaciente().getId() : null,
                    s.getPaciente() != null ? s.getPaciente().getNome() : "Não Identificado",
                    s.getPaciente() != null ? s.getPaciente().getCpf() : "",
                    s.getPaciente() != null ? s.getPaciente().getTelefone() : "",
                    s.getStatus(),
                    s.getEstadoConversa(),
                    s.getDataInicio(),
                    s.getDataUltimaInteracao(),
                    ultimaMsg
            );
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/sessoes/{id}/mensagens")
    public ResponseEntity<List<MensagemDTO>> listarMensagens(@PathVariable Long id) {
        List<Mensagem> msgs = mensagemRepository.findBySessaoIdOrderByDataHoraAsc(id);
        List<MensagemDTO> dtos = msgs.stream().map(m -> new MensagemDTO(
                m.getId(),
                m.getSessao().getId(),
                m.getRemetente(),
                m.getConteudo(),
                m.getDataHora(),
                m.getLida()
        )).toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/sessoes/{id}/assumir")
    @Transactional
    public ResponseEntity<Void> assumirAtendimento(@PathVariable Long id) {
        SessaoAtendimento sessao = sessaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + id));

        sessao.setStatus(StatusSessao.EM_ATENDIMENTO_HUMANO);
        sessao.setEstadoConversa(EstadoConversa.ATENDIMENTO_HUMANO);
        sessao.setDataUltimaInteracao(LocalDateTime.now());
        sessaoRepository.save(sessao);

        // Notificar o paciente
        String aviso = "👩‍💼 *Você está falando com a nossa recepção humana.* Como posso te ajudar?";
        Mensagem msg = new Mensagem(sessao, RemetenteTipo.ATENDENTE, aviso);
        mensagemRepository.save(msg);

        if (sessao.getCanal() == CanalTipo.TELEGRAM) {
            telegramBotService.enviarMensagem(sessao.getCanalUsuarioId(), aviso);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/sessoes/{id}/enviar-mensagem")
    @Transactional
    public ResponseEntity<MensagemDTO> enviarMensagemAtendente(@PathVariable Long id,
                                                              @Valid @RequestBody NovaMensagemDTO dto) {
        SessaoAtendimento sessao = sessaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + id));

        sessao.setDataUltimaInteracao(LocalDateTime.now());
        sessaoRepository.save(sessao);

        Mensagem msg = new Mensagem(sessao, RemetenteTipo.ATENDENTE, dto.conteudo());
        msg = mensagemRepository.save(msg);

        if (sessao.getCanal() == CanalTipo.TELEGRAM) {
            telegramBotService.enviarMensagem(sessao.getCanalUsuarioId(), "👩‍💼 *Atendente:* " + dto.conteudo());
        }

        return ResponseEntity.ok(new MensagemDTO(
                msg.getId(),
                sessao.getId(),
                msg.getRemetente(),
                msg.getConteudo(),
                msg.getDataHora(),
                msg.getLida()
        ));
    }

    @PostMapping("/sessoes/{id}/devolver-bot")
    @Transactional
    public ResponseEntity<Void> devolverParaBot(@PathVariable Long id) {
        SessaoAtendimento sessao = sessaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + id));

        sessao.setStatus(StatusSessao.BOT);
        sessao.setEstadoConversa(EstadoConversa.MENU_PRINCIPAL);
        sessao.setDataUltimaInteracao(LocalDateTime.now());
        sessaoRepository.save(sessao);

        String aviso = "🤖 O atendimento humano foi finalizado e você retornou ao assistente virtual. Digite *menu* a qualquer momento.";
        Mensagem msg = new Mensagem(sessao, RemetenteTipo.BOT, aviso);
        mensagemRepository.save(msg);

        if (sessao.getCanal() == CanalTipo.TELEGRAM) {
            telegramBotService.enviarMensagem(sessao.getCanalUsuarioId(), aviso);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/sessoes/{id}/finalizar")
    @Transactional
    public ResponseEntity<Void> finalizar(@PathVariable Long id) {
        SessaoAtendimento sessao = sessaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + id));

        sessao.setStatus(StatusSessao.FINALIZADO);
        sessao.setDataUltimaInteracao(LocalDateTime.now());
        sessaoRepository.save(sessao);

        String aviso = "✅ Atendimento concluído. Obrigado pelo contato com a OmniMed Care! Tenha um ótimo dia.";
        Mensagem msg = new Mensagem(sessao, RemetenteTipo.SISTEMA, aviso);
        mensagemRepository.save(msg);

        if (sessao.getCanal() == CanalTipo.TELEGRAM) {
            telegramBotService.enviarMensagem(sessao.getCanalUsuarioId(), aviso);
        }

        return ResponseEntity.ok().build();
    }
}
