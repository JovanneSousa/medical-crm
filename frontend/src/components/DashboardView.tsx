import React from 'react';
import { 
  CalendarCheck2, 
  Bot, 
  Users, 
  CheckCircle2, 
  MessageSquareWarning,
  Send,
  Globe,
  Share2
} from 'lucide-react';
import type { DashboardStats } from '../types';

interface DashboardViewProps {
  stats: DashboardStats | null;
  onNavigateToLiveChat: () => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({ stats, onNavigateToLiveChat }) => {
  return (
    <div className="p-8 space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Painel Executivo da Clínica</h2>
          <p className="text-sm text-slate-500 mt-1">
            Monitoramento em tempo real dos fluxos omnichannel, autoatendimento e transbordo humano.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 border border-emerald-200">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            Plataforma Operacional (24/7)
          </span>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Card 1 */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Agendamentos Totais</span>
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
              <CalendarCheck2 className="w-5 h-5" />
            </div>
          </div>
          <p className="text-3xl font-bold text-slate-900 mt-4">{stats?.totalAgendamentos ?? 0}</p>
          <div className="flex items-center gap-1.5 mt-2 text-xs text-emerald-600 font-medium">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>{stats?.agendamentosConfirmados ?? 0} confirmados</span>
          </div>
        </div>

        {/* Card 2 */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Resolução Automática (Bot)</span>
            <div className="w-10 h-10 rounded-xl bg-teal-50 text-teal-600 flex items-center justify-center">
              <Bot className="w-5 h-5" />
            </div>
          </div>
          <p className="text-3xl font-bold text-teal-600 mt-4">{stats?.taxaRetencaoBot ?? 0}%</p>
          <p className="mt-2 text-xs text-slate-500">
            Atendimentos concluídos sem intervenção humana
          </p>
        </div>

        {/* Card 3 */}
        <div 
          onClick={onNavigateToLiveChat}
          className="bg-white p-6 rounded-2xl border border-amber-200/80 shadow-sm hover:shadow-md transition-shadow cursor-pointer relative overflow-hidden"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-amber-700 uppercase tracking-wider">Fila Transbordo Humano</span>
            <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center">
              <MessageSquareWarning className="w-5 h-5" />
            </div>
          </div>
          <p className="text-3xl font-bold text-amber-600 mt-4">{stats?.aguardandoHumano ?? 0}</p>
          <p className="mt-2 text-xs text-amber-700 font-medium hover:underline">
            Pacientes aguardando recepção →
          </p>
        </div>

        {/* Card 4 */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Corpo Clínico & Pacientes</span>
            <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
              <Users className="w-5 h-5" />
            </div>
          </div>
          <p className="text-3xl font-bold text-slate-900 mt-4">{stats?.totalPacientes ?? 0}</p>
          <p className="mt-2 text-xs text-slate-500">
            {stats?.totalMedicos ?? 0} médicos ativos em escala
          </p>
        </div>
      </div>

      {/* Omnichannel Gateways Status */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm">
        <h3 className="text-base font-bold text-slate-900 mb-4 flex items-center gap-2">
          <Share2 className="w-5 h-5 text-teal-600" />
          Status dos Portais e Canais de Entrada
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="p-4 rounded-xl border border-slate-100 bg-slate-50/60 flex items-start gap-4">
            <div className="w-10 h-10 rounded-lg bg-sky-500 text-white flex items-center justify-center font-bold text-lg shadow-sm">
              <Send className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h4 className="font-semibold text-slate-900 text-sm">Telegram Bot API</h4>
                <span className="px-2 py-0.5 text-[10px] font-bold rounded-full bg-emerald-100 text-emerald-800">
                  ATIVO
                </span>
              </div>
              <p className="text-xs text-slate-500 mt-1">
                Conexão com Telegram para agendamento guiado e suporte.
              </p>
            </div>
          </div>

          <div className="p-4 rounded-xl border border-slate-100 bg-slate-50/60 flex items-start gap-4">
            <div className="w-10 h-10 rounded-lg bg-teal-600 text-white flex items-center justify-center font-bold text-lg shadow-sm">
              <Globe className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h4 className="font-semibold text-slate-900 text-sm">WebChat Widget</h4>
                <span className="px-2 py-0.5 text-[10px] font-bold rounded-full bg-emerald-100 text-emerald-800">
                  INTEGRADO
                </span>
              </div>
              <p className="text-xs text-slate-500 mt-1">
                Portal web para pacientes acessarem consultas e laudos.
              </p>
            </div>
          </div>

          <div className="p-4 rounded-xl border border-slate-100 bg-slate-50/60 flex items-start gap-4">
            <div className="w-10 h-10 rounded-lg bg-emerald-600 text-white flex items-center justify-center font-bold text-lg shadow-sm">
              W
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h4 className="font-semibold text-slate-900 text-sm">WhatsApp Cloud API</h4>
                <span className="px-2 py-0.5 text-[10px] font-bold rounded-full bg-slate-200 text-slate-700">
                  ROADMAP FASE 2
                </span>
              </div>
              <p className="text-xs text-slate-500 mt-1">
                Preparado para Meta Cloud API com mesma arquitetura.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
