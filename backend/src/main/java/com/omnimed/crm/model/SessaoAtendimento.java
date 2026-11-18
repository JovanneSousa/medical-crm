package com.omnimed.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Representa uma sessão de atendimento omnichannel com um paciente ou visitante.
 * Encapsula as transições de estado da máquina conversacional e dados temporários de fluxo.
 */
@Entity
@Table(name = "sessoes_atendimento")
public class SessaoAtendimento extends EntidadeBase {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CanalTipo canal;

    @Column(name = "canal_usuario_id", nullable = false, length = 100)
    private String canalUsuarioId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusSessao status;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conversa", nullable = false, length = 40)
    private EstadoConversa estadoConversa;

    @Column(name = "temp_especialidade_id")
    private Long tempEspecialidadeId;

    @Column(name = "temp_medico_id")
    private Long tempMedicoId;

    @Column(name = "temp_cpf", length = 20)
    private String tempCpf;

    @Column(name = "temp_nome", length = 150)
    private String tempNome;

    @Column(name = "temp_telefone", length = 30)
    private String tempTelefone;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_ultima_interacao", nullable = false)
    private LocalDateTime dataUltimaInteracao;

    public SessaoAtendimento() {
        this.dataInicio = LocalDateTime.now();
        this.dataUltimaInteracao = LocalDateTime.now();
        this.status = StatusSessao.BOT;
        this.estadoConversa = EstadoConversa.INICIO;
    }

    public SessaoAtendimento(CanalTipo canal, String canalUsuarioId) {
        this();
        this.canal = canal;
        this.canalUsuarioId = canalUsuarioId;
    }

    // --- Métodos de encapsulamento do ciclo de vida da sessão ---

    public void registrarInteracao() {
        this.dataUltimaInteracao = LocalDateTime.now();
    }

    public void reabrirSeFinalizada() {
        if (this.status == StatusSessao.FINALIZADO) {
            this.status = StatusSessao.BOT;
            this.estadoConversa = (this.paciente != null)
                    ? EstadoConversa.MENU_PRINCIPAL
                    : EstadoConversa.AGUARDANDO_CPF;
        }
    }

    public void limparDadosTemporarios() {
        this.tempEspecialidadeId = null;
        this.tempMedicoId = null;
        this.tempCpf = null;
        this.tempNome = null;
        this.tempTelefone = null;
    }

    public void reiniciarFluxoParaInicio() {
        limparDadosTemporarios();
        this.status = StatusSessao.BOT;
        this.estadoConversa = (this.paciente != null)
                ? EstadoConversa.MENU_PRINCIPAL
                : EstadoConversa.AGUARDANDO_CPF;
    }

    public void identificarPaciente(Paciente paciente) {
        this.paciente = paciente;
        this.estadoConversa = EstadoConversa.MENU_PRINCIPAL;
        limparDadosTemporarios();
    }

    public void iniciarCadastroComCpf(String cpfLimpo) {
        this.tempCpf = cpfLimpo;
        this.estadoConversa = EstadoConversa.CADASTRO_NOME;
    }

    public void registrarNomeCadastro(String nome) {
        this.tempNome = nome;
        this.estadoConversa = EstadoConversa.CADASTRO_TELEFONE;
    }

    public void registrarTelefoneCadastro(String telefone) {
        this.tempTelefone = telefone;
        this.estadoConversa = EstadoConversa.CADASTRO_EMAIL;
    }

    public void concluirCadastro(Paciente novoPaciente) {
        identificarPaciente(novoPaciente);
    }

    public void iniciarEscolhaEspecialidade() {
        this.estadoConversa = EstadoConversa.ESCOLHENDO_ESPECIALIDADE;
    }

    public void selecionarEspecialidade(Long especialidadeId) {
        this.tempEspecialidadeId = especialidadeId;
        this.estadoConversa = EstadoConversa.ESCOLHENDO_MEDICO;
    }

    public void selecionarMedico(Long medicoId) {
        this.tempMedicoId = medicoId;
        this.estadoConversa = EstadoConversa.ESCOLHENDO_HORARIO;
    }

    public void retornarAoMenuPrincipal() {
        this.tempEspecialidadeId = null;
        this.tempMedicoId = null;
        this.estadoConversa = EstadoConversa.MENU_PRINCIPAL;
    }

    public void iniciarConsultaAgendamentos() {
        this.estadoConversa = EstadoConversa.CONSULTANDO_AGENDAMENTOS;
    }

    public void iniciarConsultaExames() {
        this.estadoConversa = EstadoConversa.CONSULTANDO_EXAME;
    }

    public void transbordarParaAtendente() {
        this.status = StatusSessao.AGUARDANDO_HUMANO;
        this.estadoConversa = EstadoConversa.ATENDIMENTO_HUMANO;
    }

    public void assumirPorAtendente() {
        this.status = StatusSessao.EM_ATENDIMENTO_HUMANO;
        this.estadoConversa = EstadoConversa.ATENDIMENTO_HUMANO;
        registrarInteracao();
    }

    public void devolverParaBot() {
        this.status = StatusSessao.BOT;
        this.estadoConversa = EstadoConversa.MENU_PRINCIPAL;
        registrarInteracao();
    }

    public void finalizar() {
        this.status = StatusSessao.FINALIZADO;
        this.estadoConversa = EstadoConversa.FINALIZADO;
        limparDadosTemporarios();
        registrarInteracao();
    }

    @JsonIgnore
    public boolean isEmAtendimentoHumano() {
        return this.status == StatusSessao.EM_ATENDIMENTO_HUMANO;
    }

    @JsonIgnore
    public boolean isFinalizada() {
        return this.status == StatusSessao.FINALIZADO;
    }

    // --- Getters e Setters ---

    public CanalTipo getCanal() {
        return canal;
    }

    public void setCanal(CanalTipo canal) {
        this.canal = canal;
    }

    public String getCanalUsuarioId() {
        return canalUsuarioId;
    }

    public void setCanalUsuarioId(String canalUsuarioId) {
        this.canalUsuarioId = canalUsuarioId;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public StatusSessao getStatus() {
        return status;
    }

    public void setStatus(StatusSessao status) {
        this.status = status;
    }

    public EstadoConversa getEstadoConversa() {
        return estadoConversa;
    }

    public void setEstadoConversa(EstadoConversa estadoConversa) {
        this.estadoConversa = estadoConversa;
    }

    public Long getTempEspecialidadeId() {
        return tempEspecialidadeId;
    }

    public void setTempEspecialidadeId(Long tempEspecialidadeId) {
        this.tempEspecialidadeId = tempEspecialidadeId;
    }

    public Long getTempMedicoId() {
        return tempMedicoId;
    }

    public void setTempMedicoId(Long tempMedicoId) {
        this.tempMedicoId = tempMedicoId;
    }

    public String getTempCpf() {
        return tempCpf;
    }

    public void setTempCpf(String tempCpf) {
        this.tempCpf = tempCpf;
    }

    public String getTempNome() {
        return tempNome;
    }

    public void setTempNome(String tempNome) {
        this.tempNome = tempNome;
    }

    public String getTempTelefone() {
        return tempTelefone;
    }

    public void setTempTelefone(String tempTelefone) {
        this.tempTelefone = tempTelefone;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataUltimaInteracao() {
        return dataUltimaInteracao;
    }

    public void setDataUltimaInteracao(LocalDateTime dataUltimaInteracao) {
        this.dataUltimaInteracao = dataUltimaInteracao;
    }
}
