import React, { useState, useEffect } from 'react';
import { 
  Send, 
  Bot, 
  RotateCcw, 
  Sparkles, 
  AlertTriangle, 
  MessageCircle, 
  Globe, 
  UserPlus, 
  LogOut, 
  Copy, 
  Check, 
  Smartphone,
  QrCode,
  Wifi,
  WifiOff,
  X
} from 'lucide-react';
import { api } from '../services/api';
import type { CanalTipo } from '../types';

interface MsgSimulador {
  id: number;
  remetente: 'paciente' | 'bot';
  texto: string;
  hora: string;
}

export const SimuladorView: React.FC = () => {
  const [canalAtivo, setCanalAtivo] = useState<CanalTipo>('WHATSAPP');
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
  const [sessionId, setSessionId] = useState(`whatsapp_sim_${Date.now()}`);
  const [copiado, setCopiado] = useState(false);

  // Status do Gateway Evolution API
  const [gatewayStatus, setGatewayStatus] = useState<{
    online: boolean;
    conectado: boolean;
    connectionState: string;
    gatewayBaseUrl?: string;
  } | null>(null);
  const [checandoGateway, setChecandoGateway] = useState(false);

  // Modal QR Code
  const [qrModalAberto, setQrModalAberto] = useState(false);
  const [qrCodeBase64, setQrCodeBase64] = useState<string | null>(null);
  const [carregandoQr, setCarregandoQr] = useState(false);
  const [qrErro, setQrErro] = useState<string | null>(null);

  const checarStatusGateway = async () => {
    try {
      setChecandoGateway(true);
      const data = await api.getWhatsAppGatewayStatus();
      setGatewayStatus(data);
    } catch {
      setGatewayStatus({ online: false, conectado: false, connectionState: 'offline' });
    } finally {
      setChecandoGateway(false);
    }
  };

  useEffect(() => {
    checarStatusGateway();
  }, []);

  const abrirModalConectar = async () => {
    setQrModalAberto(true);
    setCarregandoQr(true);
    setQrErro(null);
    setQrCodeBase64(null);

    try {
      const res = await api.conectarWhatsAppGateway();
      if (res.sucesso && res.base64) {
        setQrCodeBase64(res.base64);
      } else if (res.erro) {
        setQrErro(res.erro);
      } else {
        setQrErro(res.mensagem || 'Instância pronta ou já conectada.');
      }
      await checarStatusGateway();
    } catch (err: any) {
      setQrErro(err.message || 'Erro ao conectar com o gateway Evolution API na porta 8081.');
    } finally {
      setCarregandoQr(false);
    }
  };

  const trocarCanal = (novoCanal: CanalTipo) => {
    setCanalAtivo(novoCanal);
    const novoId = `${novoCanal.toLowerCase()}_sim_${Date.now()}`;
    setSessionId(novoId);
    setMensagens([
      {
        id: Date.now(),
        remetente: 'bot',
        texto: `👋 Olá! Bem-vindo à Clínica OmniMed Care (via ${novoCanal}).\n\nSou seu assistente virtual de atendimento integrado.\nPara localizar seu cadastro com segurança, por favor digite seu CPF:`,
        hora: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
  };

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
      const res = await api.enviarMensagemSimulador(texto, sessionId, canalAtivo);
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
    const novoId = `${canalAtivo.toLowerCase()}_sim_${Date.now()}`;
    setSessionId(novoId);
    setMensagens([
      {
        id: Date.now(),
        remetente: 'bot',
        texto: '👋 Olá! Bem-vindo à Clínica OmniMed Care.\n\nSou seu assistente virtual de atendimento integrado.\nPara localizar seu cadastro com segurança, por favor digite seu CPF:',
        hora: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
  };

  const copiarWebhook = () => {
    const url = `${window.location.protocol}//${window.location.host}/api/webhook/whatsapp`;
    navigator.clipboard.writeText(url);
    setCopiado(true);
    setTimeout(() => setCopiado(false), 2000);
  };

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Simulador Interativo Omnichannel</h2>
          <p className="text-sm text-slate-500 mt-1">
            Teste em tempo real os fluxos de atendimento, cadastro via bot, encerramento pelo cliente e integração com WhatsApp, Telegram e Web.
          </p>
        </div>

        {/* Seletor de Canal */}
        <div className="flex items-center gap-1.5 p-1 bg-white rounded-xl border border-slate-200 shadow-xs">
          <button
            onClick={() => trocarCanal('WHATSAPP')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 cursor-pointer transition-all ${
              canalAtivo === 'WHATSAPP'
                ? 'bg-emerald-600 text-white shadow-xs'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            <MessageCircle className="w-3.5 h-3.5" />
            WhatsApp
          </button>
          <button
            onClick={() => trocarCanal('TELEGRAM')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 cursor-pointer transition-all ${
              canalAtivo === 'TELEGRAM'
                ? 'bg-sky-600 text-white shadow-xs'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            <Send className="w-3.5 h-3.5" />
            Telegram
          </button>
          <button
            onClick={() => trocarCanal('WEB_CHAT')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 cursor-pointer transition-all ${
              canalAtivo === 'WEB_CHAT'
                ? 'bg-teal-700 text-white shadow-xs'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            <Globe className="w-3.5 h-3.5" />
            Web Chat
          </button>
        </div>
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

            {/* App Header customizado por Canal */}
            <div className={`p-3.5 rounded-2xl flex items-center justify-between text-white shadow-sm mb-3 transition-colors ${
              canalAtivo === 'WHATSAPP' ? 'bg-[#075E54]' : canalAtivo === 'TELEGRAM' ? 'bg-[#229ED9]' : 'bg-teal-700'
            }`}>
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-full bg-white/20 flex items-center justify-center font-bold text-sm">
                  {canalAtivo === 'WHATSAPP' ? <MessageCircle className="w-5 h-5 text-white" /> : <Bot className="w-5 h-5 text-white" />}
                </div>
                <div>
                  <h4 className="font-bold text-sm leading-tight">
                    {canalAtivo === 'WHATSAPP' ? 'OmniMed Care (WhatsApp)' : canalAtivo === 'TELEGRAM' ? 'OmniMed Care Bot' : 'Recepção Virtual'}
                  </h4>
                  <p className="text-[10px] text-white/80">
                    {canalAtivo === 'WHATSAPP' ? 'online • Conta comercial verificada' : 'Assistente Virtual Integrado'}
                  </p>
                </div>
              </div>
              <button
                onClick={handleReset}
                title="Reiniciar Sessão"
                className="p-1.5 rounded-lg bg-black/20 hover:bg-black/30 text-white transition-colors cursor-pointer"
              >
                <RotateCcw className="w-4 h-4" />
              </button>
            </div>

            {/* Chat Body */}
            <div className={`h-[480px] rounded-2xl p-4 overflow-y-auto space-y-3 flex flex-col ${
              canalAtivo === 'WHATSAPP' ? 'bg-[#E5DDD5]' : 'bg-slate-100'
            }`}>
              {mensagens.map((msg) => (
                <div
                  key={msg.id}
                  className={`flex flex-col ${msg.remetente === 'paciente' ? 'items-end' : 'items-start'}`}
                >
                  <div
                    className={`max-w-[85%] p-3 rounded-2xl text-xs leading-relaxed whitespace-pre-wrap shadow-sm ${
                      msg.remetente === 'paciente'
                        ? canalAtivo === 'WHATSAPP'
                          ? 'bg-[#DCF8C6] text-slate-900 rounded-tr-none'
                          : 'bg-teal-600 text-white rounded-tr-none'
                        : 'bg-white text-slate-800 border border-slate-200/80 rounded-tl-none font-sans'
                    }`}
                  >
                    {msg.texto}
                  </div>
                  <span className="text-[9px] text-slate-500 mt-0.5 px-1">{msg.hora}</span>
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
                className={`px-4 py-2.5 rounded-xl text-slate-950 font-bold text-xs transition-colors disabled:opacity-50 cursor-pointer ${
                  canalAtivo === 'WHATSAPP' ? 'bg-[#25D366] hover:bg-[#1ebd5a]' : 'bg-teal-400 hover:bg-teal-300'
                }`}
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        </div>

        {/* Painel Lateral com Atalhos e Guia WhatsApp */}
        <div className="lg:col-span-5 space-y-4">
          {/* Card de Conexão com o Gateway Docker (Evolution API) */}
          <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2">
                <Smartphone className="w-4 h-4 text-emerald-600" />
                Gateway WhatsApp (Docker)
              </h3>
              <button
                onClick={checarStatusGateway}
                disabled={checandoGateway}
                title="Checar status"
                className="p-1 text-slate-400 hover:text-slate-600 cursor-pointer"
              >
                <RotateCcw className={`w-3.5 h-3.5 ${checandoGateway ? 'animate-spin' : ''}`} />
              </button>
            </div>

            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-200 text-xs">
              <div className="flex items-center gap-2">
                {gatewayStatus?.online ? (
                  <Wifi className="w-4 h-4 text-emerald-600" />
                ) : (
                  <WifiOff className="w-4 h-4 text-rose-500" />
                )}
                <div>
                  <p className="font-semibold text-slate-800">
                    {gatewayStatus?.online ? 'Evolution API Online' : 'Evolution API Offline'}
                  </p>
                  <p className="text-[11px] text-slate-500">
                    Porta: 8081 • Instância: <code>omnimed</code>
                  </p>
                </div>
              </div>

              <span className={`px-2 py-0.5 rounded-full font-semibold text-[10px] ${
                gatewayStatus?.conectado
                  ? 'bg-emerald-100 text-emerald-800'
                  : gatewayStatus?.online
                  ? 'bg-amber-100 text-amber-800'
                  : 'bg-slate-200 text-slate-600'
              }`}>
                {gatewayStatus?.conectado ? 'Conectado' : gatewayStatus?.online ? 'Aguardando QR Code' : 'Docker Offline'}
              </span>
            </div>

            {/* Ações de Conexão */}
            <div className="flex items-center gap-2">
              <button
                onClick={abrirModalConectar}
                className="flex-1 py-2 px-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs flex items-center justify-center gap-1.5 shadow-sm cursor-pointer transition-colors"
              >
                <QrCode className="w-3.5 h-3.5" />
                Conectar / Ler QR Code
              </button>

              <button
                onClick={copiarWebhook}
                className="py-2 px-3 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-700 font-semibold text-xs flex items-center gap-1 cursor-pointer transition-colors"
              >
                {copiado ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5 text-slate-400" />}
                Webhook
              </button>
            </div>

            <p className="text-[11px] text-slate-400 leading-tight">
              Para subir o gateway via Docker, execute no terminal do projeto: <code className="text-slate-700 font-mono">docker-compose up -d</code>.
            </p>
          </div>

          {/* Cenários Rápidos de Teste */}
          <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm space-y-3">
            <h3 className="font-bold text-base text-slate-900 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-teal-600" />
              Atalhos dos Novos Fluxos
            </h3>
            <p className="text-xs text-slate-500">
              Clique nos botões abaixo para testar diretamente os novos requisitos implementados:
            </p>

            <div className="space-y-2">
              {/* Teste 1: Paciente cadastrado */}
              <button
                onClick={() => handleEnviar(undefined, '123.456.789-00')}
                className="w-full text-left p-3 rounded-xl border border-slate-200 hover:border-teal-400 hover:bg-teal-50/50 transition-all text-xs cursor-pointer"
              >
                <p className="font-semibold text-slate-800">1. Identificar com CPF Existente</p>
                <p className="text-slate-500 text-[11px] mt-0.5">Envia CPF de João da Silva e carrega o menu completo.</p>
              </button>

              {/* Teste 2: Cadastro pelo Chatbot */}
              <button
                onClick={() => handleEnviar(undefined, '111.999.888-77')}
                className="w-full text-left p-3 rounded-xl border border-emerald-300 bg-emerald-50/40 hover:bg-emerald-100/50 transition-all text-xs cursor-pointer"
              >
                <p className="font-semibold text-emerald-950 flex items-center gap-1.5">
                  <UserPlus className="w-3.5 h-3.5 text-emerald-600" />
                  2. Testar Cadastro pelo Chatbot (CPF Novo)
                </p>
                <p className="text-emerald-800 text-[11px] mt-0.5">
                  Simula CPF não cadastrado; o bot inicia o fluxo interativo solicitando Nome, Telefone e E-mail!
                </p>
              </button>

              {/* Teste 3: Encerrar Atendimento */}
              <button
                onClick={() => handleEnviar(undefined, 'encerrar')}
                className="w-full text-left p-3 rounded-xl border border-indigo-200 bg-indigo-50/40 hover:bg-indigo-100/50 transition-all text-xs cursor-pointer"
              >
                <p className="font-semibold text-indigo-950 flex items-center gap-1.5">
                  <LogOut className="w-3.5 h-3.5 text-indigo-600" />
                  3. Encerrar Atendimento pelo Cliente
                </p>
                <p className="text-indigo-800 text-[11px] mt-0.5">
                  Envia comando 'encerrar' (ou opção 6 do menu) finalizando a conversa sem precisar de atendente humano.
                </p>
              </button>

              {/* Teste 4: Transbordo Humano */}
              <button
                onClick={() => handleEnviar(undefined, '5')}
                className="w-full text-left p-3 rounded-xl border border-amber-200 bg-amber-50/40 hover:bg-amber-100/50 transition-all text-xs cursor-pointer"
              >
                <p className="font-semibold text-amber-950">4. Solicitar Recepção Humana (Opção 5)</p>
                <p className="text-amber-800 text-[11px] mt-0.5">Envia o chamado para a fila do Inbox Omnichannel.</p>
              </button>

              {/* Teste 5: Emergência SAMU */}
              <button
                onClick={() => handleEnviar(undefined, 'socorro dor no peito e falta de ar')}
                className="w-full text-left p-3 rounded-xl border border-rose-200 bg-rose-50/40 hover:bg-rose-100/50 transition-all text-xs cursor-pointer"
              >
                <p className="font-semibold text-rose-950 flex items-center gap-1.5">
                  <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />
                  5. Testar Limite Ético (Emergência Médica)
                </p>
                <p className="text-rose-800 text-[11px] mt-0.5">Aciona diretriz de segurança e orienta SAMU (192).</p>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Modal QR Code WhatsApp */}
      {qrModalAberto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
          <div className="bg-white w-full max-w-sm rounded-2xl shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
            <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
                <QrCode className="w-4 h-4 text-emerald-600" />
                Conectar WhatsApp do Celular
              </h3>
              <button
                onClick={() => setQrModalAberto(false)}
                className="text-slate-400 hover:text-slate-600 cursor-pointer p-1 rounded-lg"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-6 text-center space-y-4">
              {carregandoQr ? (
                <div className="py-12 space-y-2">
                  <div className="w-8 h-8 border-3 border-emerald-500 border-t-transparent rounded-full animate-spin mx-auto" />
                  <p className="text-xs text-slate-500">Conectando à Evolution API e gerando QR Code...</p>
                </div>
              ) : qrCodeBase64 ? (
                <div className="space-y-3">
                  <div className="p-3 bg-white border border-slate-200 rounded-2xl inline-block shadow-xs">
                    <img
                      src={qrCodeBase64.startsWith('data:') ? qrCodeBase64 : `data:image/png;base64,${qrCodeBase64}`}
                      alt="QR Code WhatsApp"
                      className="w-56 h-56 mx-auto"
                    />
                  </div>
                  <p className="text-xs text-slate-600">
                    Abra o WhatsApp no seu celular ➡️ <strong>Aparelhos conectados</strong> ➡️ <strong>Conectar aparelho</strong> e aponte para o QR Code acima.
                  </p>
                </div>
              ) : (
                <div className="py-6 space-y-3">
                  <AlertTriangle className="w-10 h-10 text-amber-500 mx-auto" />
                  <p className="text-xs text-slate-700 font-medium">
                    {qrErro || 'Certifique-se de que o container do Docker foi iniciado com "docker-compose up -d".'}
                  </p>
                  <p className="text-[11px] text-slate-400">
                    Porta esperada: <code>http://localhost:8081</code>
                  </p>
                </div>
              )}

              <div className="pt-2 border-t border-slate-100 flex justify-end">
                <button
                  onClick={() => setQrModalAberto(false)}
                  className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold cursor-pointer"
                >
                  Fechar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
