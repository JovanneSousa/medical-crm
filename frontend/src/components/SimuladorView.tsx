import React, { useState } from 'react';
import { Send, Bot, RotateCcw, Sparkles, AlertTriangle } from 'lucide-react';
import { api } from '../services/api';

interface MsgSimulador {
  id: number;
  remetente: 'paciente' | 'bot';
  texto: string;
  hora: string;
}

export const SimuladorView: React.FC = () => {
  const [mensagens, setMensagens] = useState<MsgSimulador[]>([
    {
      id: 1,
      remetente: 'bot',
      texto: '👋 Olá! Bem-vindo à Clínica OmniMed Care.\n\nSou seu assistente virtual de atendimento integrado.\nPara localizar seu cadastro com segurança, por favor digite seu CPF:',
      hora: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [inputTexto, setInputTexto] = useState('');
  const [carregando, setCarregando] = useState(false);
  const [sessionId] = useState(`simulador_${Date.now()}`);

  const handleEnviar = async (e?: React.FormEvent, textoCustom?: string) => {
    if (e) e.preventDefault();
    const texto = (textoCustom || inputTexto).trim();
    if (!texto || carregando) return;

    const horaAtual = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    setMensagens((prev) => [
      ...prev,
      { id: Date.now(), remetente: 'paciente', texto, hora: horaAtual },
    ]);
    setInputTexto('');
    setCarregando(true);

    try {
      const res = await api.enviarMensagemSimulador(texto, sessionId);
      setMensagens((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          remetente: 'bot',
          texto: res.resposta,
          hora: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        },
      ]);
    } catch {
      setMensagens((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          remetente: 'bot',
          texto: 'Erro de comunicação com o servidor de atendimento.',
          hora: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        },
      ]);
    } finally {
      setCarregando(false);
    }
  };

  const handleReset = () => {
    setMensagens([
      {
        id: 1,
        remetente: 'bot',
        texto: '👋 Olá! Bem-vindo à Clínica OmniMed Care.\n\nSou seu assistente virtual de atendimento integrado.\nPara localizar seu cadastro com segurança, por favor digite seu CPF:',
        hora: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
  };

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Simulador Interativo do Paciente</h2>
        <p className="text-sm text-slate-500 mt-1">
          Teste o fluxo transacional do chatbot (Telegram / Web) em tempo real, conectado diretamente ao backend Java.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Celular Mockup */}
        <div className="lg:col-span-7 flex justify-center">
          <div className="w-full max-w-md bg-slate-900 p-4 rounded-[40px] shadow-2xl border-4 border-slate-700">
            {/* Top Bar Mockup */}
            <div className="h-6 flex items-center justify-between px-6 text-white text-[11px] font-semibold mb-2">
              <span>09:41</span>
              <div className="w-16 h-4 bg-black rounded-full" />
              <span>5G 100%</span>
            </div>

            {/* App Header */}
            <div className="bg-teal-700 p-3.5 rounded-2xl flex items-center justify-between text-white shadow-sm mb-3">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-full bg-white/20 flex items-center justify-center font-bold text-sm">
                  <Bot className="w-5 h-5 text-white" />
                </div>
                <div>
                  <h4 className="font-bold text-sm leading-tight">OmniMed Care Bot</h4>
                  <p className="text-[10px] text-teal-200">Canal Integrado de Atendimento</p>
                </div>
              </div>
              <button
                onClick={handleReset}
                title="Reiniciar Simulação"
                className="p-1.5 rounded-lg bg-teal-800/80 hover:bg-teal-800 text-teal-100 transition-colors"
              >
                <RotateCcw className="w-4 h-4" />
              </button>
            </div>

            {/* Chat Body */}
            <div className="bg-slate-100 h-[480px] rounded-2xl p-4 overflow-y-auto space-y-3 flex flex-col">
              {mensagens.map((msg) => (
                <div
                  key={msg.id}
                  className={`flex flex-col ${msg.remetente === 'paciente' ? 'items-end' : 'items-start'}`}
                >
                  <div
                    className={`max-w-[85%] p-3 rounded-2xl text-xs leading-relaxed whitespace-pre-wrap shadow-sm ${
                      msg.remetente === 'paciente'
                        ? 'bg-teal-600 text-white rounded-tr-none'
                        : 'bg-white text-slate-800 border border-slate-200/80 rounded-tl-none font-sans'
                    }`}
                  >
                    {msg.texto}
                  </div>
                  <span className="text-[9px] text-slate-400 mt-0.5 px-1">{msg.hora}</span>
                </div>
              ))}
              {carregando && (
                <div className="flex items-center gap-1 text-slate-400 text-xs p-2">
                  <span className="w-2 h-2 rounded-full bg-slate-400 animate-bounce"></span>
                  <span className="w-2 h-2 rounded-full bg-slate-400 animate-bounce delay-100"></span>
                  <span className="w-2 h-2 rounded-full bg-slate-400 animate-bounce delay-200"></span>
                </div>
              )}
            </div>

            {/* Input Bar */}
            <form onSubmit={(e) => handleEnviar(e)} className="mt-3 flex gap-2">
              <input
                type="text"
                value={inputTexto}
                onChange={(e) => setInputTexto(e.target.value)}
                placeholder="Digite sua resposta..."
                className="flex-1 px-4 py-2.5 rounded-xl bg-slate-800 border border-slate-700 text-white text-xs placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-teal-500"
              />
              <button
                type="submit"
                disabled={carregando || !inputTexto.trim()}
                className="px-4 py-2.5 rounded-xl bg-teal-500 text-slate-950 font-bold text-xs hover:bg-teal-400 transition-colors disabled:opacity-50"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        </div>

        {/* Guia de Teste Rápido */}
        <div className="lg:col-span-5 space-y-4">
          <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm">
            <h3 className="font-bold text-base text-slate-900 mb-2 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-teal-600" />
              Atalhos de Teste Imediato
            </h3>
            <p className="text-xs text-slate-500 mb-4">
              Clique em um dos cenários abaixo para testar instantaneamente os fluxos de negócio:
            </p>

            <div className="space-y-2.5">
              <button
                onClick={() => handleEnviar(undefined, '123.456.789-00')}
                className="w-full text-left p-3 rounded-xl border border-slate-200 hover:border-teal-400 hover:bg-teal-50/50 transition-all text-xs"
              >
                <p className="font-semibold text-slate-800">1. Identificar com CPF Cadastrado</p>
                <p className="text-slate-500 text-[11px] mt-0.5">Envia CPF de João da Silva e carrega menu</p>
              </button>

              <button
                onClick={() => handleEnviar(undefined, '1')}
                className="w-full text-left p-3 rounded-xl border border-slate-200 hover:border-teal-400 hover:bg-teal-50/50 transition-all text-xs"
              >
                <p className="font-semibold text-slate-800">2. Iniciar Agendamento de Consulta</p>
                <p className="text-slate-500 text-[11px] mt-0.5">Lista as especialidades médicas do banco</p>
              </button>

              <button
                onClick={() => handleEnviar(undefined, '3')}
                className="w-full text-left p-3 rounded-xl border border-slate-200 hover:border-teal-400 hover:bg-teal-50/50 transition-all text-xs"
              >
                <p className="font-semibold text-slate-800">3. Consultar Meus Exames e Laudos</p>
                <p className="text-slate-500 text-[11px] mt-0.5">Busca laudos e exames prontos do paciente</p>
              </button>

              <button
                onClick={() => handleEnviar(undefined, '5')}
                className="w-full text-left p-3 rounded-xl border border-amber-300 bg-amber-50/40 hover:bg-amber-100/50 transition-all text-xs"
              >
                <p className="font-semibold text-amber-900">4. Solicitar Transbordo para Recepção (Opção 5)</p>
                <p className="text-amber-700 text-[11px] mt-0.5">
                  Transfere a conversa para o Inbox Omnichannel do operador humano
                </p>
              </button>

              <button
                onClick={() => handleEnviar(undefined, 'socorro dor no peito e falta de ar')}
                className="w-full text-left p-3 rounded-xl border border-rose-200 bg-rose-50/40 hover:bg-rose-100/50 transition-all text-xs"
              >
                <p className="font-semibold text-rose-900 flex items-center gap-1.5">
                  <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />
                  5. Testar Limite Ético (Emergência Médica)
                </p>
                <p className="text-rose-700 text-[11px] mt-0.5">
                  Valida a recusa ética de diagnóstico e alerta 192 (SAMU)
                </p>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
