import { useState, useEffect } from 'react';
import { Sidebar } from './components/Sidebar';
import { DashboardView } from './components/DashboardView';
import { LiveChatView } from './components/LiveChatView';
import { AgendamentosView } from './components/AgendamentosView';
import { MedicosView } from './components/MedicosView';
import { SimuladorView } from './components/SimuladorView';
import type { DashboardStats } from './types';
import { api } from './services/api';

export function App() {
  const [currentTab, setCurrentTab] = useState<string>('dashboard');
  const [stats, setStats] = useState<DashboardStats | null>(null);

  const carregarStats = async () => {
    try {
      const data = await api.getDashboardStats();
      setStats(data);
    } catch (err) {
      console.error('Falha ao carregar métricas:', err);
    }
  };

  useEffect(() => {
    carregarStats();
    const interval = setInterval(carregarStats, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="flex h-screen bg-slate-100 overflow-hidden font-sans">
      {/* Sidebar de Navegação */}
      <Sidebar
        currentTab={currentTab}
        setCurrentTab={setCurrentTab}
        filaAguardandoCount={stats?.aguardandoHumano ?? 0}
      />

      {/* Área Central de Conteúdo */}
      <main className="flex-1 overflow-y-auto">
        {currentTab === 'dashboard' && (
          <DashboardView
            stats={stats}
            onNavigateToLiveChat={() => setCurrentTab('livechat')}
          />
        )}
        {currentTab === 'livechat' && <LiveChatView />}
        {currentTab === 'agendamentos' && <AgendamentosView />}
        {currentTab === 'medicos' && <MedicosView />}
        {currentTab === 'simulador' && <SimuladorView />}
      </main>
    </div>
  );
}

export default App;
