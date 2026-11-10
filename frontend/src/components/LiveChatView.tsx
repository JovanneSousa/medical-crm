import React, { useState, useEffect } from 'react';
import { 
  Send, 
  UserCheck, 
  RotateCcw, 
  CheckCircle, 
  Clock, 
  MessageSquare, 
  AlertCircle
} from 'lucide-react';
import type { Mensagem, SessaoAtendimento, StatusSessao } from '../types';
import { api } from '../services/api';

export const LiveChatView: React.FC = () => {
  const [sessoes, setSessoes] = useState<SessaoAtendimento[]>([]);
  const [sessaoSelecionada, setSessaoSelecionada] = useState<SessaoAtendimento | null>(null);
  const [mensagens, setMensagens] = useState<Mensagem[]>([]);
  const [novoTexto, setNovoTexto] = useState('');
  const [filtroStatus, setFiltroStatus] = useState<string>('TODOS');
  const [carregando, setCarregando] = useState(false);

  const carregarSessoes = async () => {
    try {
      const statusParam = filtroStatus === 'TODOS' ? undefined : (filtroStatus as StatusSessao);
      const data = await api.getSessoes(statusParam);
      setSessoes(data);
      if (data.length > 0 && !sessaoSelecionada) {
        setSessaoSelecionada(data[0]);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const carregarMensagens = async (sessaoId: number) => {
    try {
      const msgs = await api.getMensagens(sessaoId);
      setMensagens(msgs);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    carregarSessoes();
    const interval = setInterval(carregarSessoes, 4000);
    return () => clearInterval(interval);
  }, [filtroStatus]);

  useEffect(() => {
    if (sessaoSelecionada) {
      carregarMensagens(sessaoSelecionada.id);
      const interval = setInterval(() => carregarMensagens(sessaoSelecionada.id), 3000);
      return () => clearInterval(interval);
    }
  }, [sessaoSelecionada]);

  const handleEnviarMensagem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sessaoSelecionada || !novoTexto.trim()) return;

    try {
      setCarregando(true);
      await api.enviarMensagemAtendente(sessaoSelecionada.id, novoTexto);
      setNovoTexto('');
      await carregarMensagens(sessaoSelecionada.id);
      await carregarSessoes();
    } catch (err) {
      alert('Erro ao enviar mensagem.');
    } finally {
      setCarregando(false);
    }
  };

  const handleAssumir = async () => {
    if (!sessaoSelecionada) return;
    try {
      await api.assumirAtendimento(sessaoSelecionada.id);
      await carregarSessoes();
      await carregarMensagens(sessaoSelecionada.id);
    } catch (err) {
      alert('Erro ao assumir atendimento.');
    }
  };

  const handleDevolverBot = async () => {
    if (!sessaoSelecionada) return;
    try {
      await api.devolverParaBot(sessaoSelecionada.id);
      await carregarSessoes();
      await carregarMensagens(sessaoSelecionada.id);
    } catch (err) {
      alert('Erro ao devolver para o bot.');
    }
  };

  const handleFinalizar = async () => {
    if (!sessaoSelecionada) return;
    try {
      await api.finalizarAtendimento(sessaoSelecionada.id);
      await carregarSessoes();
      await carregarMensagens(sessaoSelecionada.id);
    } catch (err) {
      alert('Erro ao finalizar atendimento.');
    }
  };

  return (
    <div className="h-[calc(100vh-2rem)] flex border border-slate-200 bg-white rounded-2xl overflow-hidden m-4 shadow-sm">
      {/* 1. Lista Lateral de Conversas */}
      <div className="w-80 md:w-96 border-r border-slate-200 flex flex-col bg-slate-50/50">
        <div className="p-4 border-b border-slate-200">
          <div className="flex items-center justify-between mb-3">
            <h3 className="font-bold text-slate-900 text-base">Inbox de Atendimentos</h3>
            <span className="text-xs px-2 py-0.5 rounded-full bg-slate-200 text-slate-700 font-semibold">
              {sessoes.length} ativas
            </span>
          </div>
          {/* Filtros */}
          <div className="flex gap-1 overflow-x-auto pb-1 text-xs">
            {['TODOS', 'AGUARDANDO_HUMANO', 'EM_ATENDIMENTO_HUMANO', 'BOT', 'FINALIZADO'].map((status) => (
              <button
                key={status}
                onClick={() => setFiltroStatus(status)}
                className={`px-2.5 py-1 rounded-lg font-medium whitespace-nowrap transition-colors ${
                  filtroStatus === status
                    ? 'bg-teal-600 text-white'
                    : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-100'
                }`}
              >
                {status === 'TODOS' && 'Todos'}
                {status === 'AGUARDANDO_HUMANO' && 'Fila de Espera'}
                {status === 'EM_ATENDIMENTO_HUMANO' && 'Em Atendimento'}
                {status === 'BOT' && 'Autoatendimento (Bot)'}
                {status === 'FINALIZADO' && 'Finalizados'}
              </button>
            ))}
          </div>
        </div>

        {/* Lista de Sessões */}
        <div className="flex-1 overflow-y-auto divide-y divide-slate-100">
          {sessoes.length === 0 ? (
            <div className="p-8 text-center text-slate-400 text-xs">
              Nenhuma sessão de atendimento encontrada para este filtro.
            </div>
          ) : (
            sessoes.map((s) => {
              const isSelected = sessaoSelecionada?.id === s.id;
              const isAguardando = s.status === 'AGUARDANDO_HUMANO';
              return (
                <div
                  key={s.id}
                  onClick={() => setSessaoSelecionada(s)}
                  className={`p-3.5 cursor-pointer transition-all ${
                    isSelected ? 'bg-teal-50/80 border-l-4 border-teal-600' : 'hover:bg-slate-100/60'
                  }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <span className={`w-2 h-2 rounded-full ${
                        isAguardando ? 'bg-amber-500 animate-ping' : s.status === 'EM_ATENDIMENTO_HUMANO' ? 'bg-emerald-500' : 'bg-slate-400'
                      }`} />
                      <h4 className="font-semibold text-sm text-slate-900 truncate">
                        {s.pacienteNome || 'Paciente Anônimo'}
                      </h4>
                    </div>
                    <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-200 text-slate-700">
                      {s.canal}
                    </span>
                  </div>

                  <p className="text-xs text-slate-500 truncate mt-1">
                    {s.ultimaMensagem || 'Iniciando atendimento...'}
                  </p>

                  <div className="flex items-center justify-between mt-2 pt-1 border-t border-slate-100 text-[11px]">
                    <span className={`font-semibold px-2 py-0.5 rounded-full ${
                      isAguardando
                        ? 'bg-amber-100 text-amber-800'
                        : s.status === 'EM_ATENDIMENTO_HUMANO'
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-slate-100 text-slate-600'
                    }`}>
                      {s.status === 'AGUARDANDO_HUMANO' ? 'Aguardando Operador' : s.status}
                    </span>
                    <span className="text-slate-400 flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {new Date(s.dataUltimaInteracao).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* 2. Chat Principal */}
      <div className="flex-1 flex flex-col bg-white">
        {sessaoSelecionada ? (
          <>
            {/* Header da Conversa */}
            <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/40">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-teal-100 text-teal-800 flex items-center justify-center font-bold">
                  {sessaoSelecionada.pacienteNome?.charAt(0) || 'P'}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-bold text-slate-900 text-sm">
                      {sessaoSelecionada.pacienteNome}
                    </h3>
                    <span className="px-2 py-0.5 text-[10px] font-bold rounded-full bg-slate-100 text-slate-700">
                      Canal: {sessaoSelecionada.canal}
                    </span>
                  </div>
                  <p className="text-xs text-slate-500">
                    CPF: {sessaoSelecionada.pacienteCpf || 'Não informado'} | Tel: {sessaoSelecionada.pacienteTelefone || 'N/A'}
                  </p>
                </div>
              </div>

              {/* Botões de Ação do Operador */}
              <div className="flex items-center gap-2">
                {sessaoSelecionada.status === 'AGUARDANDO_HUMANO' && (
                  <button
                    onClick={handleAssumir}
                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600 text-white text-xs font-semibold hover:bg-emerald-700 transition-colors shadow-sm"
                  >
                    <UserCheck className="w-3.5 h-3.5" />
                    Assumir Atendimento
                  </button>
                )}

                {sessaoSelecionada.status === 'EM_ATENDIMENTO_HUMANO' && (
                  <>
                    <button
                      onClick={handleDevolverBot}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-100 border border-slate-300 text-slate-700 text-xs font-semibold hover:bg-slate-200 transition-colors"
                    >
                      <RotateCcw className="w-3.5 h-3.5" />
                      Devolver ao Bot
                    </button>
                    <button
                      onClick={handleFinalizar}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-teal-700 text-white text-xs font-semibold hover:bg-teal-800 transition-colors"
                    >
                      <CheckCircle className="w-3.5 h-3.5" />
                      Finalizar
                    </button>
                  </>
                )}
              </div>
            </div>

            {/* Mensagens */}
            <div className="flex-1 overflow-y-auto p-6 space-y-4 bg-slate-50/30">
              {mensagens.map((msg) => {
                const isPaciente = msg.remetente === 'PACIENTE';
                const isBot = msg.remetente === 'BOT';
                const isAtendente = msg.remetente === 'ATENDENTE';

                return (
                  <div
                    key={msg.id}
                    className={`flex flex-col ${isPaciente ? 'items-start' : 'items-end'}`}
                  >
                    <div className="flex items-center gap-1.5 text-[11px] text-slate-400 mb-1 px-1">
                      <span className="font-semibold text-slate-600">
                        {isPaciente && 'Paciente'}
                        {isBot && '🤖 Assistente OmniMed'}
                        {isAtendente && '👩‍💼 Atendente Humano'}
                      </span>
                      <span>•</span>
                      <span>{new Date(msg.dataHora).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                    </div>

                    <div
                      className={`max-w-xl p-3.5 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap shadow-sm ${
                        isPaciente
                          ? 'bg-white border border-slate-200 text-slate-800 rounded-tl-sm'
                          : isAtendente
                          ? 'bg-teal-700 text-white rounded-tr-sm'
                          : 'bg-teal-50 border border-teal-200 text-teal-950 rounded-tr-sm font-sans'
                      }`}
                    >
                      {msg.conteudo}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Input para resposta */}
            <div className="p-4 border-t border-slate-200 bg-white">
              {sessaoSelecionada.status === 'EM_ATENDIMENTO_HUMANO' ? (
                <form onSubmit={handleEnviarMensagem} className="flex gap-2">
                  <input
                    type="text"
                    value={novoTexto}
                    onChange={(e) => setNovoTexto(e.target.value)}
                    placeholder="Digite sua resposta para o paciente (será enviada ao canal ativo)..."
                    className="flex-1 px-4 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
                    disabled={carregando}
                  />
                  <button
                    type="submit"
                    disabled={carregando || !novoTexto.trim()}
                    className="px-5 py-2.5 rounded-xl bg-teal-600 text-white font-semibold text-sm hover:bg-teal-700 transition-colors disabled:opacity-50 flex items-center gap-2"
                  >
                    <Send className="w-4 h-4" />
                    Enviar
                  </button>
                </form>
              ) : (
                <div className="p-3 rounded-xl bg-slate-100 text-slate-600 text-xs text-center flex items-center justify-center gap-2">
                  <AlertCircle className="w-4 h-4 text-amber-500" />
                  <span>
                    Esta sessão está sob controle do <strong>assistente automático (Bot)</strong>. Para responder manualmente, clique em <strong>Assumir Atendimento</strong> acima.
                  </span>
                </div>
              )}
            </div>
          </>
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center p-8 text-slate-400">
            <MessageSquare className="w-12 h-12 mb-3 stroke-1" />
            <p className="text-sm">Selecione uma conversa ao lado para visualizar o atendimento.</p>
          </div>
        )}
      </div>

      {/* 3. Resumo Lateral do Paciente */}
      {sessaoSelecionada && (
        <div className="w-72 border-l border-slate-200 bg-white p-5 hidden lg:flex flex-col justify-between">
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4">
              Dados do Paciente
            </h4>
            <div className="space-y-4">
              <div>
                <label className="text-[11px] text-slate-400 block">Nome Completo</label>
                <p className="text-sm font-semibold text-slate-800">{sessaoSelecionada.pacienteNome}</p>
              </div>
              <div>
                <label className="text-[11px] text-slate-400 block">CPF</label>
                <p className="text-sm font-mono text-slate-800">{sessaoSelecionada.pacienteCpf || 'Não cadastrado'}</p>
              </div>
              <div>
                <label className="text-[11px] text-slate-400 block">Telefone</label>
                <p className="text-sm text-slate-800">{sessaoSelecionada.pacienteTelefone || 'Não informado'}</p>
              </div>
              <div>
                <label className="text-[11px] text-slate-400 block">Canal Ativo</label>
                <p className="text-sm font-semibold text-teal-600">{sessaoSelecionada.canal}</p>
              </div>
            </div>
          </div>

          <div className="p-3 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-600">
            <p className="font-semibold text-slate-800 mb-1">Status de Privacidade</p>
            Conforme a LGPD, o histórico desta conversa é criptografado e auditado.
          </div>
        </div>
      )}
    </div>
  );
};
