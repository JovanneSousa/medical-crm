import type { Agendamento, DashboardStats, Medico, Mensagem, SessaoAtendimento, StatusSessao } from '../types';

const BASE_URL = '/api';

export const api = {
  async getDashboardStats(): Promise<DashboardStats> {
    const res = await fetch(`${BASE_URL}/dashboard/stats`);
    if (!res.ok) throw new Error('Erro ao carregar estatísticas');
    return res.json();
  },

  async getAgendamentos(): Promise<Agendamento[]> {
    const res = await fetch(`${BASE_URL}/agendamentos`);
    if (!res.ok) throw new Error('Erro ao buscar agendamentos');
    return res.json();
  },

  async cancelarAgendamento(id: number): Promise<Agendamento> {
    const res = await fetch(`${BASE_URL}/agendamentos/${id}/cancelar`, { method: 'POST' });
    if (!res.ok) throw new Error('Erro ao cancelar agendamento');
    return res.json();
  },

  async getSessoes(status?: StatusSessao): Promise<SessaoAtendimento[]> {
    const url = status ? `${BASE_URL}/atendimentos/sessoes?status=${status}` : `${BASE_URL}/atendimentos/sessoes`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Erro ao carregar sessões');
    return res.json();
  },

  async getMensagens(sessaoId: number): Promise<Mensagem[]> {
    const res = await fetch(`${BASE_URL}/atendimentos/sessoes/${sessaoId}/mensagens`);
    if (!res.ok) throw new Error('Erro ao carregar mensagens');
    return res.json();
  },

  async assumirAtendimento(sessaoId: number): Promise<void> {
    const res = await fetch(`${BASE_URL}/atendimentos/sessoes/${sessaoId}/assumir`, { method: 'POST' });
    if (!res.ok) throw new Error('Erro ao assumir atendimento');
  },

  async enviarMensagemAtendente(sessaoId: number, conteudo: string): Promise<Mensagem> {
    const res = await fetch(`${BASE_URL}/atendimentos/sessoes/${sessaoId}/enviar-mensagem`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ conteudo }),
    });
    if (!res.ok) throw new Error('Erro ao enviar mensagem');
    return res.json();
  },

  async devolverParaBot(sessaoId: number): Promise<void> {
    const res = await fetch(`${BASE_URL}/atendimentos/sessoes/${sessaoId}/devolver-bot`, { method: 'POST' });
    if (!res.ok) throw new Error('Erro ao devolver para o bot');
  },

  async finalizarAtendimento(sessaoId: number): Promise<void> {
    const res = await fetch(`${BASE_URL}/atendimentos/sessoes/${sessaoId}/finalizar`, { method: 'POST' });
    if (!res.ok) throw new Error('Erro ao finalizar atendimento');
  },

  async getMedicos(): Promise<Medico[]> {
    const res = await fetch(`${BASE_URL}/medicos`);
    if (!res.ok) throw new Error('Erro ao listar médicos');
    return res.json();
  },

  async enviarMensagemSimulador(conteudo: string, usuarioId: string = 'web_paciente_demo'): Promise<{ resposta: string }> {
    const res = await fetch(`${BASE_URL}/simulador/mensagem?canal=WEB_CHAT&usuarioId=${encodeURIComponent(usuarioId)}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ conteudo }),
    });
    if (!res.ok) throw new Error('Erro ao processar mensagem no simulador');
    return res.json();
  }
};
