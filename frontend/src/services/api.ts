import type { 
  Agendamento, 
  DashboardStats, 
  Especialidade, 
  Medico, 
  MedicoRequest, 
  Mensagem, 
  Paciente, 
  PacienteRequest, 
  SessaoAtendimento, 
  StatusSessao 
} from '../types';

const BASE_URL = '/api';

export const api = {
  // Dashboard
  async getDashboardStats(): Promise<DashboardStats> {
    const res = await fetch(`${BASE_URL}/dashboard/stats`);
    if (!res.ok) throw new Error('Erro ao carregar estatísticas');
    return res.json();
  },

  // Agendamentos
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

  // Live Chat / Atendimentos
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

  // Corpo Clínico (Médicos)
  async getMedicos(apenasAtivos?: boolean): Promise<Medico[]> {
    const url = apenasAtivos ? `${BASE_URL}/medicos?apenasAtivos=true` : `${BASE_URL}/medicos`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Erro ao listar médicos');
    return res.json();
  },

  async createMedico(data: MedicoRequest): Promise<Medico> {
    const res = await fetch(`${BASE_URL}/medicos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) {
      const err = await res.text();
      throw new Error(err || 'Erro ao cadastrar médico');
    }
    return res.json();
  },

  async updateMedico(id: number, data: MedicoRequest): Promise<Medico> {
    const res = await fetch(`${BASE_URL}/medicos/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) {
      const err = await res.text();
      throw new Error(err || 'Erro ao atualizar médico');
    }
    return res.json();
  },

  async deleteMedico(id: number): Promise<{ mensagem?: string; ativo?: boolean } | null> {
    const res = await fetch(`${BASE_URL}/medicos/${id}`, { method: 'DELETE' });
    if (!res.ok) {
      const err = await res.text();
      throw new Error(err || 'Erro ao remover médico');
    }
    if (res.status === 204) return null;
    return res.json();
  },

  async getEspecialidades(): Promise<Especialidade[]> {
    const res = await fetch(`${BASE_URL}/especialidades`);
    if (!res.ok) throw new Error('Erro ao buscar especialidades');
    return res.json();
  },

  // Pacientes (CRUD)
  async getPacientes(busca?: string): Promise<Paciente[]> {
    const url = busca ? `${BASE_URL}/pacientes?busca=${encodeURIComponent(busca)}` : `${BASE_URL}/pacientes`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Erro ao carregar pacientes');
    return res.json();
  },

  async getPaciente(id: number): Promise<Paciente> {
    const res = await fetch(`${BASE_URL}/pacientes/${id}`);
    if (!res.ok) throw new Error('Erro ao buscar paciente');
    return res.json();
  },

  async createPaciente(data: PacienteRequest): Promise<Paciente> {
    const res = await fetch(`${BASE_URL}/pacientes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) {
      const json = await res.json().catch(() => null);
      throw new Error(json?.mensagem || 'Erro ao cadastrar paciente');
    }
    return res.json();
  },

  async updatePaciente(id: number, data: PacienteRequest): Promise<Paciente> {
    const res = await fetch(`${BASE_URL}/pacientes/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) {
      const json = await res.json().catch(() => null);
      throw new Error(json?.mensagem || 'Erro ao atualizar paciente');
    }
    return res.json();
  },

  async deletePaciente(id: number): Promise<void> {
    const res = await fetch(`${BASE_URL}/pacientes/${id}`, { method: 'DELETE' });
    if (!res.ok) {
      const json = await res.json().catch(() => null);
      throw new Error(json?.mensagem || 'Erro ao excluir paciente');
    }
  },

  // Simulador / Chatbot Demo
  async enviarMensagemSimulador(
    conteudo: string, 
    usuarioId: string = 'web_paciente_demo',
    canal: string = 'WEB_CHAT'
  ): Promise<{ resposta: string }> {
    const res = await fetch(`${BASE_URL}/simulador/mensagem?canal=${canal}&usuarioId=${encodeURIComponent(usuarioId)}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ conteudo }),
    });
    if (!res.ok) throw new Error('Erro ao processar mensagem no simulador');
    return res.json();
  },

  // WhatsApp Webhook Tester / Simulador
  async testarWhatsApp(from: string, message: string): Promise<{ resposta: string; from: string }> {
    const res = await fetch(`${BASE_URL}/webhook/whatsapp/simular`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ from, message }),
    });
    if (!res.ok) throw new Error('Erro no teste de webhook WhatsApp');
    const data = await res.json();
    return { resposta: data.resposta, from: data.usuarioId };
  },

  async getWhatsAppGatewayStatus(): Promise<{
    online: boolean;
    connectionState: string;
    conectado: boolean;
    gatewayBaseUrl: string;
    instanceName: string;
    motivo?: string;
  }> {
    const res = await fetch(`${BASE_URL}/webhook/whatsapp/status`);
    if (!res.ok) throw new Error('Erro ao consultar status do gateway WhatsApp');
    return res.json();
  },

  async conectarWhatsAppGateway(): Promise<{
    sucesso: boolean;
    base64?: string;
    code?: string;
    pairingCode?: string;
    mensagem?: string;
    erro?: string;
  }> {
    const res = await fetch(`${BASE_URL}/webhook/whatsapp/conectar`, { method: 'POST' });
    if (!res.ok) throw new Error('Erro ao conectar ao gateway WhatsApp');
    return res.json();
  }
};
