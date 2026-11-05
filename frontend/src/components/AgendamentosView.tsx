import React, { useState, useEffect } from 'react';
import { User, Stethoscope, Ban, Clock } from 'lucide-react';
import type { Agendamento } from '../types';
import { api } from '../services/api';

export const AgendamentosView: React.FC = () => {
  const [agendamentos, setAgendamentos] = useState<Agendamento[]>([]);

  const carregar = async () => {
    try {
      const data = await api.getAgendamentos();
      setAgendamentos(data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    carregar();
  }, []);

  const handleCancelar = async (id: number) => {
    if (!confirm('Deseja realmente cancelar este agendamento?')) return;
    try {
      await api.cancelarAgendamento(id);
      await carregar();
    } catch (err) {
      alert('Erro ao cancelar agendamento.');
    }
  };

  return (
    <div className="p-8 space-y-6 max-w-7xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Grade de Agendamentos Médicos</h2>
          <p className="text-sm text-slate-500 mt-1">
            Consultas agendadas via bot omnichannel (Telegram / Web) e integradas à agenda clínica.
          </p>
        </div>
        <button
          onClick={carregar}
          className="px-4 py-2 rounded-xl border border-slate-200 bg-white text-xs font-semibold text-slate-700 hover:bg-slate-50 shadow-sm"
        >
          Atualizar Lista
        </button>
      </div>

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
              {agendamentos.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-10 text-slate-400">
                    Nenhum agendamento cadastrado.
                  </td>
                </tr>
              ) : (
                agendamentos.map((item) => {
                  const isCancelado = item.status === 'CANCELADO';
                  return (
                    <tr key={item.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="px-6 py-4 font-mono font-bold text-xs text-teal-700">
                        {item.protocolo}
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2">
                          <User className="w-4 h-4 text-slate-400" />
                          <div>
                            <p className="font-semibold text-slate-900 text-sm">{item.pacienteNome}</p>
                            <p className="text-xs text-slate-400 font-mono">CPF: {item.pacienteCpf || 'N/A'}</p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2">
                          <Stethoscope className="w-4 h-4 text-teal-600" />
                          <div>
                            <p className="font-medium text-slate-800 text-sm">{item.medicoNome}</p>
                            <span className="text-xs text-slate-500">{item.especialidadeNome}</span>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-1.5 text-slate-700 text-xs">
                          <Clock className="w-3.5 h-3.5 text-slate-400" />
                          <span className="font-medium">
                            {new Date(item.dataHora).toLocaleDateString('pt-BR')} às{' '}
                            {new Date(item.dataHora).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <span className="px-2 py-0.5 rounded-md text-[11px] font-mono font-semibold bg-slate-100 text-slate-700 border border-slate-200">
                          {item.canalOrigem}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold ${
                          isCancelado
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-emerald-100 text-emerald-800'
                        }`}>
                          {isCancelado ? 'Cancelado' : 'Confirmado'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        {!isCancelado && (
                          <button
                            onClick={() => handleCancelar(item.id)}
                            className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-rose-200 text-rose-700 hover:bg-rose-50 text-xs font-semibold transition-colors"
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
