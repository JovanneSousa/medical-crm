import React, { useState, useEffect, useMemo } from 'react';
import { 
  User, 
  Stethoscope, 
  Ban, 
  Clock, 
  Search, 
  Filter, 
  RotateCcw, 
  Calendar, 
  CheckCircle, 
  XCircle,
  MessageCircle,
  Send,
  Globe
} from 'lucide-react';
import type { Agendamento, CanalTipo } from '../types';
import { api } from '../services/api';

export const AgendamentosView: React.FC = () => {
  const [agendamentos, setAgendamentos] = useState<Agendamento[]>([]);
  const [carregando, setCarregando] = useState(false);

  // Estados dos Filtros
  const [buscaTexto, setBuscaTexto] = useState('');
  const [filtroMedico, setFiltroMedico] = useState('');
  const [filtroEspecialidade, setFiltroEspecialidade] = useState('');
  const [filtroData, setFiltroData] = useState('');
  const [filtroStatus, setFiltroStatus] = useState<string>('TODOS');
  const [filtroCanal, setFiltroCanal] = useState<string>('TODOS');

  const carregar = async () => {
    try {
      setCarregando(true);
      const data = await api.getAgendamentos();
      setAgendamentos(data);
    } catch (err) {
      console.error(err);
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    carregar();
  }, []);

  const handleCancelar = async (id: number) => {
    if (!confirm('Deseja realmente cancelar este agendamento? O horário será liberado na agenda do médico.')) return;
    try {
      await api.cancelarAgendamento(id);
      await carregar();
    } catch (err) {
      alert('Erro ao cancelar agendamento.');
    }
  };

  // Listas únicas para os selects dos filtros
  const medicosDisponiveis = useMemo(() => {
    const nomes = new Set<string>();
    agendamentos.forEach((a) => { if (a.medicoNome) nomes.add(a.medicoNome); });
    return Array.from(nomes).sort();
  }, [agendamentos]);

  const especialidadesDisponiveis = useMemo(() => {
    const esps = new Set<string>();
    agendamentos.forEach((a) => { if (a.especialidadeNome) esps.add(a.especialidadeNome); });
    return Array.from(esps).sort();
  }, [agendamentos]);

  // Aplicação dos Filtros
  const agendamentosFiltrados = useMemo(() => {
    return agendamentos.filter((item) => {
      // 1. Busca textual (Protocolo, Nome do Paciente, CPF)
      if (buscaTexto.trim()) {
        const termo = buscaTexto.toLowerCase().trim();
        const bateProtocolo = item.protocolo.toLowerCase().includes(termo);
        const bateNome = item.pacienteNome.toLowerCase().includes(termo);
        const bateCpf = item.pacienteCpf?.toLowerCase().includes(termo) ?? false;
        if (!bateProtocolo && !bateNome && !bateCpf) return false;
      }

      // 2. Filtro de Médico
      if (filtroMedico && item.medicoNome !== filtroMedico) {
        return false;
      }

      // 3. Filtro de Especialidade
      if (filtroEspecialidade && item.especialidadeNome !== filtroEspecialidade) {
        return false;
      }

      // 4. Filtro de Data (YYYY-MM-DD)
      if (filtroData) {
        const itemData = item.dataHora.split('T')[0];
        if (itemData !== filtroData) return false;
      }

      // 5. Filtro de Status
      if (filtroStatus !== 'TODOS' && item.status !== filtroStatus) {
        return false;
      }

      // 6. Filtro de Canal
      if (filtroCanal !== 'TODOS' && item.canalOrigem !== filtroCanal) {
        return false;
      }

      return true;
    });
  }, [agendamentos, buscaTexto, filtroMedico, filtroEspecialidade, filtroData, filtroStatus, filtroCanal]);

  const limparFiltros = () => {
    setBuscaTexto('');
    setFiltroMedico('');
    setFiltroEspecialidade('');
    setFiltroData('');
    setFiltroStatus('TODOS');
    setFiltroCanal('TODOS');
  };

  const temFiltroAtivo = buscaTexto !== '' || filtroMedico !== '' || filtroEspecialidade !== '' || filtroData !== '' || filtroStatus !== 'TODOS' || filtroCanal !== 'TODOS';

  const formatarDataHoje = () => {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = String(hoje.getMonth() + 1).padStart(2, '0');
    const dia = String(hoje.getDate()).padStart(2, '0');
    setFiltroData(`${ano}-${mes}-${dia}`);
  };

  const getCanalBadge = (canal: CanalTipo) => {
    switch (canal) {
      case 'TELEGRAM':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-sky-50 text-sky-700 border border-sky-200">
            <Send className="w-3 h-3" />
            Telegram
          </span>
        );
      case 'WHATSAPP':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
            <MessageCircle className="w-3 h-3" />
            WhatsApp
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-semibold bg-slate-100 text-slate-700 border border-slate-200">
            <Globe className="w-3 h-3" />
            Web Chat
          </span>
        );
    }
  };

  return (
    <div className="p-8 space-y-6 max-w-7xl mx-auto">
      {/* Cabeçalho */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Grade de Agendamentos Médicos</h2>
          <p className="text-sm text-slate-500 mt-1">
            Consultas agendadas via bot omnichannel (Telegram, WhatsApp e Web) e integradas à agenda clínica.
          </p>
        </div>
        <button
          onClick={carregar}
          disabled={carregando}
          className="px-4 py-2 rounded-xl border border-slate-200 bg-white text-xs font-semibold text-slate-700 hover:bg-slate-50 shadow-sm flex items-center gap-2 cursor-pointer transition-colors"
        >
          <RotateCcw className={`w-3.5 h-3.5 ${carregando ? 'animate-spin' : ''}`} />
          Atualizar Lista
        </button>
      </div>

      {/* Barra de Filtros */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-sm font-bold text-slate-800">
            <Filter className="w-4 h-4 text-teal-600" />
            <span>Filtros de Pesquisa</span>
            <span className="ml-2 px-2 py-0.5 rounded-full text-xs bg-slate-100 text-slate-600 font-mono">
              {agendamentosFiltrados.length} de {agendamentos.length}
            </span>
          </div>

          {temFiltroAtivo && (
            <button
              onClick={limparFiltros}
              className="text-xs text-rose-600 hover:text-rose-700 font-semibold flex items-center gap-1 cursor-pointer transition-colors"
            >
              <RotateCcw className="w-3 h-3" />
              Limpar filtros
            </button>
          )}
        </div>

        {/* Inputs de Filtro em Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
          {/* Busca Texto */}
          <div className="lg:col-span-2 relative">
            <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
            <input
              type="text"
              value={buscaTexto}
              onChange={(e) => setBuscaTexto(e.target.value)}
              placeholder="Buscar por paciente, CPF ou protocolo..."
              className="w-full pl-9 pr-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500 focus:ring-1 focus:ring-teal-500"
            />
          </div>

          {/* Médico */}
          <div>
            <select
              value={filtroMedico}
              onChange={(e) => setFiltroMedico(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500 bg-white"
            >
              <option value="">Todos os Médicos</option>
              {medicosDisponiveis.map((nome) => (
                <option key={nome} value={nome}>{nome}</option>
              ))}
            </select>
          </div>

          {/* Especialidade */}
          <div>
            <select
              value={filtroEspecialidade}
              onChange={(e) => setFiltroEspecialidade(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500 bg-white"
            >
              <option value="">Todas Especialidades</option>
              {especialidadesDisponiveis.map((esp) => (
                <option key={esp} value={esp}>{esp}</option>
              ))}
            </select>
          </div>

          {/* Data */}
          <div className="flex gap-1.5">
            <input
              type="date"
              value={filtroData}
              onChange={(e) => setFiltroData(e.target.value)}
              className="w-full px-2.5 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500 bg-white"
            />
            <button
              onClick={formatarDataHoje}
              type="button"
              title="Filtrar por Hoje"
              className="px-2 py-2 text-[11px] font-semibold rounded-xl border border-slate-200 bg-slate-50 hover:bg-slate-100 text-slate-700 cursor-pointer"
            >
              Hoje
            </button>
          </div>

          {/* Status */}
          <div>
            <select
              value={filtroStatus}
              onChange={(e) => setFiltroStatus(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500 bg-white"
            >
              <option value="TODOS">Todos os Status</option>
              <option value="CONFIRMADO">Confirmados</option>
              <option value="CANCELADO">Cancelados</option>
              <option value="AGENDADO">Agendados</option>
              <option value="REALIZADO">Realizados</option>
            </select>
          </div>
        </div>

        {/* Linha extra: Filtro por Canal */}
        <div className="flex flex-wrap items-center gap-2 pt-2 border-t border-slate-100 text-xs">
          <span className="text-slate-400 font-medium mr-1">Canal de Origem:</span>
          {['TODOS', 'TELEGRAM', 'WHATSAPP', 'WEB_CHAT'].map((c) => (
            <button
              key={c}
              onClick={() => setFiltroCanal(c)}
              className={`px-3 py-1 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
                filtroCanal === c
                  ? 'bg-teal-700 text-white shadow-sm'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {c === 'TODOS' ? 'Todos os Canais' : c === 'WEB_CHAT' ? 'Web Chat' : c}
            </button>
          ))}
        </div>
      </div>

      {/* Tabela de Resultados */}
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-200">
              <tr>
                <th className="px-6 py-4">Protocolo</th>
                <th className="px-6 py-4">Paciente</th>
                <th className="px-6 py-4">Médico / Especialidade</th>
                <th className="px-6 py-4">Data e Hora</th>
                <th className="px-6 py-4">Canal</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {agendamentosFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <Calendar className="w-8 h-8 mx-auto text-slate-300 mb-2" />
                    <p className="font-medium text-slate-600 text-sm">Nenhum agendamento encontrado.</p>
                    <p className="text-xs text-slate-400 mt-1">Tente ajustar ou limpar os filtros de pesquisa.</p>
                  </td>
                </tr>
              ) : (
                agendamentosFiltrados.map((item) => {
                  const isCancelado = item.status === 'CANCELADO';
                  return (
                    <tr key={item.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="px-6 py-4 font-mono font-bold text-xs text-teal-700">
                        {item.protocolo}
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2">
                          <User className="w-4 h-4 text-slate-400 shrink-0" />
                          <div>
                            <p className="font-semibold text-slate-900 text-sm">{item.pacienteNome}</p>
                            <p className="text-xs text-slate-400 font-mono">
                              CPF: {item.pacienteCpf ? item.pacienteCpf : 'N/A'}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2">
                          <Stethoscope className="w-4 h-4 text-teal-600 shrink-0" />
                          <div>
                            <p className="font-medium text-slate-800 text-sm">{item.medicoNome}</p>
                            <span className="text-xs text-slate-500">{item.especialidadeNome}</span>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-1.5 text-slate-700 text-xs">
                          <Clock className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                          <span className="font-medium">
                            {new Date(item.dataHora).toLocaleDateString('pt-BR')} às{' '}
                            {new Date(item.dataHora).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        {getCanalBadge(item.canalOrigem)}
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold ${
                          isCancelado
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-emerald-100 text-emerald-800'
                        }`}>
                          {isCancelado ? <XCircle className="w-3 h-3" /> : <CheckCircle className="w-3 h-3" />}
                          {isCancelado ? 'Cancelado' : 'Confirmado'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        {!isCancelado && (
                          <button
                            onClick={() => handleCancelar(item.id)}
                            className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-rose-200 text-rose-700 hover:bg-rose-50 text-xs font-semibold transition-colors cursor-pointer"
                          >
                            <Ban className="w-3.5 h-3.5" />
                            Cancelar
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
