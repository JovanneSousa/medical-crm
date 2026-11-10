import React from 'react';
import { 
  LayoutDashboard, 
  MessageSquare, 
  CalendarDays, 
  Stethoscope, 
  Users,
  Bot, 
  Activity,
  ShieldCheck
} from 'lucide-react';

interface SidebarProps {
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  filaAguardandoCount: number;
}

export const Sidebar: React.FC<SidebarProps> = ({ currentTab, setCurrentTab, filaAguardandoCount }) => {
  const menuItems = [
    { id: 'dashboard', label: 'Painel Geral', icon: LayoutDashboard },
    { 
      id: 'livechat', 
      label: 'Inbox Omnichannel', 
      icon: MessageSquare,
      badge: filaAguardandoCount > 0 ? filaAguardandoCount : undefined 
    },
    { id: 'agendamentos', label: 'Agendamentos', icon: CalendarDays },
    { id: 'pacientes', label: 'Pacientes', icon: Users },
    { id: 'medicos', label: 'Corpo Clínico', icon: Stethoscope },
    { id: 'simulador', label: 'Simulador / Bot Demo', icon: Bot },
  ];

  return (
    <aside className="w-64 bg-slate-900 text-slate-100 flex flex-col justify-between shrink-0 shadow-xl">
      <div>
        {/* Brand */}
        <div className="p-6 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-teal-500/20 border border-teal-500/40 flex items-center justify-center text-teal-400">
              <Activity className="w-6 h-6 animate-pulse" />
            </div>
            <div>
              <h1 className="font-bold text-lg leading-tight tracking-wide text-white">OmniMed Care</h1>
              <p className="text-xs text-teal-400 font-medium">Plataforma Omnichannel</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="p-4 space-y-1.5">
          <p className="px-3 py-2 text-[11px] font-semibold tracking-wider text-slate-400 uppercase">
            Menu Operacional
          </p>
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setCurrentTab(item.id)}
                className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all cursor-pointer ${
                  isActive
                    ? 'bg-teal-600 text-white shadow-md shadow-teal-900/30 font-semibold'
                    : 'text-slate-300 hover:bg-slate-800/80 hover:text-white'
                }`}
              >
                <div className="flex items-center gap-3">
                  <Icon className={`w-4 h-4 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                  <span>{item.label}</span>
                </div>
                {item.badge !== undefined && (
                  <span className="px-2 py-0.5 text-xs font-bold rounded-full bg-amber-500 text-slate-950 animate-bounce">
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Footer Info */}
      <div className="p-4 border-t border-slate-800 bg-slate-950/40">
        <div className="flex items-center gap-2 text-xs text-slate-400 mb-2">
          <ShieldCheck className="w-4 h-4 text-emerald-400" />
          <span>LGPD & Ética Médica Ativos</span>
        </div>
        <p className="text-[11px] text-slate-400">
          Backend: <span className="text-teal-400 font-mono">Java 21 / Spring Boot</span>
        </p>
      </div>
    </aside>
  );
};
