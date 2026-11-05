export type CanalTipo = 'TELEGRAM' | 'WEB_CHAT' | 'WHATSAPP';
export type StatusAgendamento = 'AGENDADO' | 'CONFIRMADO' | 'CANCELADO' | 'REALIZADO';
export type StatusSessao = 'BOT' | 'AGUARDANDO_HUMANO' | 'EM_ATENDIMENTO_HUMANO' | 'FINALIZADO';
export type RemetenteTipo = 'PACIENTE' | 'BOT' | 'ATENDENTE' | 'SISTEMA';

export interface DashboardStats {
  totalAgendamentos: number;
  agendamentosConfirmados: number;
  atendimentosAtivos: number;
  aguardandoHumano: number;
  totalPacientes: number;
  taxaRetencaoBot: number;
  totalMedicos: number;
}

export interface SessaoAtendimento {
  id: number;
  canal: CanalTipo;
  canalUsuarioId: string;
  pacienteId: number | null;
  pacienteNome: string;
  pacienteCpf: string;
  pacienteTelefone: string;
  status: StatusSessao;
  estadoConversa: string;
  dataInicio: string;
  dataUltimaInteracao: string;
  ultimaMensagem: string;
}

export interface Mensagem {
  id: number;
  sessaoId: number;
  remetente: RemetenteTipo;
  conteudo: string;
  dataHora: string;
  lida: boolean;
}

export interface Agendamento {
  id: number;
  protocolo: string;
  pacienteId: number;
  pacienteNome: string;
  pacienteCpf: string;
  pacienteTelefone: string;
  medicoId: number;
  medicoNome: string;
  especialidadeNome: string;
  dataHora: string;
  status: StatusAgendamento;
  canalOrigem: CanalTipo;
  observacoes: string;
  criadoEm: string;
}

export interface Medico {
  id: number;
  nome: string;
  crm: string;
  especialidade: {
    id: number;
    nome: string;
    descricao: string;
  };
  email: string;
  telefone: string;
  ativo: boolean;
}

export interface HorarioAtendimento {
  id: number;
  data: string;
  horaInicio: string;
  horaFim: string;
  disponivel: boolean;
}
