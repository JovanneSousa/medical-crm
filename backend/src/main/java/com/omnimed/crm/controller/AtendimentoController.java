package com.omnimed.crm.controller;

import com.omnimed.crm.channel.DespachanteCanalService;
import com.omnimed.crm.dto.MensagemDTO;
import com.omnimed.crm.dto.NovaMensagemDTO;
import com.omnimed.crm.dto.SessaoDTO;
import com.omnimed.crm.model.Mensagem;
import com.omnimed.crm.model.RemetenteTipo;
import com.omnimed.crm.model.SessaoAtendimento;
import com.omnimed.crm.model.StatusSessao;
import com.omnimed.crm.repository.MensagemRepository;
import com.omnimed.crm.repository.SessaoAtendimentoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/atendimentos")
public class AtendimentoController {

    private final SessaoAtendimentoRepository sessaoRepository;
    private final MensagemRepository mensagemRepository;
    private final DespachanteCanalService despachanteCanalService;

    public AtendimentoController(SessaoAtendimentoRepository sessaoRepository,
                                 MensagemRepository mensagemRepository,
                                 DespachanteCanalService despachanteCanalService) {
        this.sessaoRepository = sessaoRepository;
        this.mensagemRepository = mensagemRepository;
        this.despachanteCanalService = despachanteCanalService;
    }

    @GetMapping("/sessoes")
    public ResponseEntity<List<SessaoDTO>> listarSessoes(@RequestParam(required = false) StatusSessao status) {
        List<SessaoAtendimento> sessoes = (status != null)
                ? sessaoRepository.findByStatusOrderByDataUltimaInteracaoDesc(status)
                : sessaoRepository.findAll();

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
        List<MensagemDTO> dtos = msgs.stream().map(this::toMensagemDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/sessoes/{id}/assumir")
    @Transactional
    public ResponseEntity<Void> assumirAtendimento(@PathVariable Long id) {
        SessaoAtendimento sessao = buscarSessao(id);
        sessao.assumirPorAtendente();
        sessaoRepository.save(sessao);

        String aviso = "👩‍💼 *Você está falando com a nossa recepção humana.* Como posso te ajudar?";
        registrarEEnviarMensagem(sessao, RemetenteTipo.ATENDENTE, aviso, aviso);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/sessoes/{id}/enviar-mensagem")
    @Transactional
    public ResponseEntity<MensagemDTO> enviarMensagemAtendente(@PathVariable Long id,
                                                              @Valid @RequestBody NovaMensagemDTO dto) {
        SessaoAtendimento sessao = buscarSessao(id);
        sessao.registrarInteracao();
        sessaoRepository.save(sessao);

        Mensagem msg = registrarEEnviarMensagem(
                sessao,
                RemetenteTipo.ATENDENTE,
                dto.conteudo(),
                "👩‍💼 *Atendente:* " + dto.conteudo()
        );

        return ResponseEntity.ok(toMensagemDTO(msg));
    }

    @PostMapping("/sessoes/{id}/devolver-bot")
    @Transactional
    public ResponseEntity<Void> devolverParaBot(@PathVariable Long id) {
        SessaoAtendimento sessao = buscarSessao(id);
        sessao.devolverParaBot();
        sessaoRepository.save(sessao);

        String aviso = "🤖 O atendimento humano foi finalizado e você retornou ao assistente virtual. Digite *menu* a qualquer momento.";
        registrarEEnviarMensagem(sessao, RemetenteTipo.BOT, aviso, aviso);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/sessoes/{id}/finalizar")
    @Transactional
    public ResponseEntity<Void> finalizar(@PathVariable Long id) {
        SessaoAtendimento sessao = buscarSessao(id);
        sessao.finalizar();
        sessaoRepository.save(sessao);

        String aviso = "✅ Atendimento concluído. Obrigado pelo contato com a OmniMed Care! Tenha um ótimo dia.";
        registrarEEnviarMensagem(sessao, RemetenteTipo.SISTEMA, aviso, aviso);

        return ResponseEntity.ok().build();
    }

    private SessaoAtendimento buscarSessao(Long id) {
        return sessaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada: " + id));
    }

    private Mensagem registrarEEnviarMensagem(SessaoAtendimento sessao,
                                            RemetenteTipo remetente,
                                            String conteudoHistorico,
                                            String conteudoCanal) {
        Mensagem msg = mensagemRepository.save(new Mensagem(sessao, remetente, conteudoHistorico));
        despachanteCanalService.enviarMensagem(sessao.getCanal(), sessao.getCanalUsuarioId(), conteudoCanal);
        return msg;
    }

    private MensagemDTO toMensagemDTO(Mensagem msg) {
        return new MensagemDTO(
                msg.getId(),
                msg.getSessao().getId(),
                msg.getRemetente(),
                msg.getConteudo(),
                msg.getDataHora(),
                msg.getLida()
        );
    }
}
