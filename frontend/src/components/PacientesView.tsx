import React, { useState, useEffect, useMemo } from 'react';
import { 
  Users, 
  Search, 
  Plus, 
  Edit3, 
  Trash2, 
  X, 
  RotateCcw, 
  Send, 
  MessageCircle, 
  Mail, 
  Phone, 
  Calendar, 
  AlertCircle,
  CheckCircle2
} from 'lucide-react';
import type { Paciente, PacienteRequest } from '../types';
import { api } from '../services/api';

export const PacientesView: React.FC = () => {
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [carregando, setCarregando] = useState(false);
  const [busca, setBusca] = useState('');

  // Modal State
  const [modalAberto, setModalAberto] = useState(false);
  const [pacienteEditando, setPacienteEditando] = useState<Paciente | null>(null);
  const [formErro, setFormErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  // Form Fields
  const [formNome, setFormNome] = useState('');
  const [formCpf, setFormCpf] = useState('');
  const [formTelefone, setFormTelefone] = useState('');
  const [formEmail, setFormEmail] = useState('');
  const [formTelegramChatId, setFormTelegramChatId] = useState('');
  const [formWhatsappNumber, setFormWhatsappNumber] = useState('');

  const carregarPacientes = async () => {
    try {
      setCarregando(true);
      const data = await api.getPacientes();
      setPacientes(data);
    } catch (err) {
      console.error('Falha ao carregar pacientes:', err);
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    carregarPacientes();
  }, []);

  const abrirModalNovo = () => {
    setPacienteEditando(null);
    setFormNome('');
    setFormCpf('');
    setFormTelefone('');
    setFormEmail('');
    setFormTelegramChatId('');
    setFormWhatsappNumber('');
    setFormErro(null);
    setModalAberto(true);
  };

  const abrirModalEditar = (p: Paciente) => {
    setPacienteEditando(p);
    setFormNome(p.nome);
    setFormCpf(p.cpf);
    setFormTelefone(p.telefone || '');
    setFormEmail(p.email || '');
    setFormTelegramChatId(p.telegramChatId || '');
    setFormWhatsappNumber(p.whatsappNumber || '');
    setFormErro(null);
    setModalAberto(true);
  };

  const fecharModal = () => {
    setModalAberto(false);
    setPacienteEditando(null);
    setFormErro(null);
  };

  const handleSalvar = async (e: React.FormEvent) => {
    e.preventDefault();
    const cpfLimpo = formCpf.replace(/\D/g, '');

    if (!formNome.trim() || cpfLimpo.length !== 11) {
      setFormErro('Informe o nome completo e um CPF válido com 11 dígitos.');
      return;
    }

    try {
      setSalvando(true);
      setFormErro(null);

      const payload: PacienteRequest = {
        nome: formNome.trim(),
        cpf: cpfLimpo,
        telefone: formTelefone.trim(),
        email: formEmail.trim(),
        telegramChatId: formTelegramChatId.trim() || null,
        whatsappNumber: formWhatsappNumber.trim() ? formWhatsappNumber.replace(/\D/g, '') : null,
      };

      if (pacienteEditando) {
        await api.updatePaciente(pacienteEditando.id, payload);
      } else {
        await api.createPaciente(payload);
      }

      fecharModal();
      await carregarPacientes();
    } catch (err: any) {
      setFormErro(err.message || 'Erro ao salvar paciente.');
    } finally {
      setSalvando(false);
    }
  };

  const handleExcluir = async (p: Paciente) => {
    if (!confirm(`Deseja realmente excluir o cadastro de ${p.nome}?`)) return;

    try {
      await api.deletePaciente(p.id);
      await carregarPacientes();
    } catch (err: any) {
      alert(err.message || 'Erro ao excluir paciente.');
    }
  };

  const formatarCpf = (cpf: string) => {
    if (!cpf || cpf.length !== 11) return cpf || 'N/A';
    return `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9, 11)}`;
  };

  // Filtro local
  const pacientesFiltrados = useMemo(() => {
    if (!busca.trim()) return pacientes;
    const termo = busca.toLowerCase().trim();
    const termoNumeros = termo.replace(/\D/g, '');

    return pacientes.filter((p) => {
      const bateNome = p.nome.toLowerCase().includes(termo);
      const bateCpf = p.cpf.includes(termoNumeros) || p.cpf.toLowerCase().includes(termo);
      const bateTelefone = p.telefone?.toLowerCase().includes(termo) ?? false;
      const bateEmail = p.email?.toLowerCase().includes(termo) ?? false;
      return bateNome || bateCpf || bateTelefone || bateEmail;
    });
  }, [pacientes, busca]);

  return (
    <div className="p-8 space-y-6 max-w-7xl mx-auto">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Gestão de Pacientes</h2>
          <p className="text-sm text-slate-500 mt-1">
            Cadastros integrados à plataforma médica, sincronizados via recepção e chatbots (Telegram, WhatsApp e Web).
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={carregarPacientes}
            disabled={carregando}
            className="px-3.5 py-2 rounded-xl border border-slate-200 bg-white text-xs font-semibold text-slate-700 hover:bg-slate-50 shadow-sm flex items-center gap-2 cursor-pointer transition-colors"
          >
            <RotateCcw className={`w-3.5 h-3.5 ${carregando ? 'animate-spin' : ''}`} />
            Atualizar
          </button>
          <button
            onClick={abrirModalNovo}
            className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-teal-600 hover:bg-teal-700 text-white font-semibold text-xs shadow-md shadow-teal-900/10 cursor-pointer transition-all"
          >
            <Plus className="w-4 h-4" />
            Novo Paciente
          </button>
        </div>
      </div>

      {/* Barra de Pesquisa */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between gap-4">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
          <input
            type="text"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Pesquisar por nome, CPF, telefone ou e-mail..."
            className="w-full pl-9 pr-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500"
          />
        </div>

        <div className="text-xs text-slate-500 font-medium">
          Total: <strong className="text-slate-800">{pacientesFiltrados.length}</strong> paciente(s)
        </div>
      </div>

      {/* Tabela de Pacientes */}
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-200">
              <tr>
                <th className="px-6 py-4">Paciente</th>
                <th className="px-6 py-4">CPF</th>
                <th className="px-6 py-4">Contatos</th>
                <th className="px-6 py-4">Canais Conectados</th>
                <th className="px-6 py-4">Consultas</th>
                <th className="px-6 py-4">Data Cadastro</th>
                <th className="px-6 py-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {pacientesFiltrados.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-slate-400">
                    <Users className="w-8 h-8 mx-auto text-slate-300 mb-2" />
                    <p className="font-semibold text-slate-700">Nenhum paciente localizado.</p>
                    <p className="text-xs text-slate-400 mt-1">Cadastre novos pacientes ou realize buscas por outros termos.</p>
                  </td>
                </tr>
              ) : (
                pacientesFiltrados.map((p) => {
                  const iniciais = p.nome
                    .split(' ')
                    .map((n) => n[0])
                    .slice(0, 2)
                    .join('')
                    .toUpperCase();

                  return (
                    <tr key={p.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-9 h-9 rounded-xl bg-teal-50 text-teal-700 font-bold text-xs flex items-center justify-center shrink-0 border border-teal-200">
                            {iniciais}
                          </div>
                          <div>
                            <p className="font-semibold text-slate-900 text-sm">{p.nome}</p>
                            <span className="text-[11px] text-slate-400 font-mono">ID: #{p.id}</span>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 font-mono text-xs font-semibold text-slate-800">
                        {formatarCpf(p.cpf)}
                      </td>
                      <td className="px-6 py-4">
                        <div className="space-y-1 text-xs">
                          {p.telefone ? (
                            <div className="flex items-center gap-1.5 text-slate-700">
                              <Phone className="w-3.5 h-3.5 text-slate-400" />
                              <span>{p.telefone}</span>
                            </div>
                          ) : (
                            <span className="text-slate-400 text-xs">Sem telefone</span>
                          )}
                          {p.email && (
                            <div className="flex items-center gap-1.5 text-slate-500">
                              <Mail className="w-3.5 h-3.5 text-slate-400" />
                              <span className="truncate max-w-[180px]">{p.email}</span>
                            </div>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex flex-wrap gap-1.5">
                          {p.telegramChatId ? (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-semibold bg-sky-50 text-sky-700 border border-sky-200">
                              <Send className="w-2.5 h-2.5" />
                              Telegram
                            </span>
                          ) : null}
                          {p.whatsappNumber ? (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                              <MessageCircle className="w-2.5 h-2.5" />
                              WhatsApp
                            </span>
                          ) : null}
                          {!p.telegramChatId && !p.whatsappNumber && (
                            <span className="text-slate-400 text-xs">Web / Presencial</span>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-700">
                          {p.totalAgendamentos} agendamento(s)
                        </span>
                      </td>
                      <td className="px-6 py-4 text-xs text-slate-500">
                        <div className="flex items-center gap-1.5">
                          <Calendar className="w-3.5 h-3.5 text-slate-400" />
                          <span>{p.dataCadastro ? new Date(p.dataCadastro).toLocaleDateString('pt-BR') : 'N/A'}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => abrirModalEditar(p)}
                            title="Editar paciente"
                            className="p-1.5 rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 cursor-pointer transition-colors"
                          >
                            <Edit3 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handleExcluir(p)}
                            title="Excluir paciente"
                            className="p-1.5 rounded-lg border border-rose-200 text-rose-600 hover:bg-rose-50 cursor-pointer transition-colors"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal de Criação / Edição de Paciente */}
      {modalAberto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
          <div className="bg-white w-full max-w-lg rounded-2xl shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
            {/* Modal Header */}
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <h3 className="font-bold text-slate-900 text-base flex items-center gap-2">
                <Users className="w-5 h-5 text-teal-600" />
                {pacienteEditando ? 'Editar Dados do Paciente' : 'Cadastrar Novo Paciente'}
              </h3>
              <button
                onClick={fecharModal}
                className="text-slate-400 hover:text-slate-600 cursor-pointer p-1 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Form */}
            <form onSubmit={handleSalvar} className="p-6 space-y-4">
              {formErro && (
                <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0 text-rose-600" />
                  <span>{formErro}</span>
                </div>
              )}

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Nome Completo do Paciente *
                </label>
                <input
                  type="text"
                  required
                  value={formNome}
                  onChange={(e) => setFormNome(e.target.value)}
                  placeholder="Ex: Mariana Silveira Costa"
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    CPF (11 dígitos) *
                  </label>
                  <input
                    type="text"
                    required
                    value={formCpf}
                    onChange={(e) => setFormCpf(e.target.value)}
                    placeholder="12345678900"
                    maxLength={14}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500 font-mono"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Telefone / Celular
                  </label>
                  <input
                    type="text"
                    value={formTelefone}
                    onChange={(e) => setFormTelefone(e.target.value)}
                    placeholder="(11) 98888-7777"
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  E-mail do Paciente
                </label>
                <input
                  type="email"
                  value={formEmail}
                  onChange={(e) => setFormEmail(e.target.value)}
                  placeholder="mariana.costa@email.com"
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500"
                />
              </div>

              {/* Integrações Omnichannel Opcionais */}
              <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200 space-y-3">
                <p className="text-xs font-bold text-slate-700 flex items-center gap-1.5">
                  <CheckCircle2 className="w-3.5 h-3.5 text-teal-600" />
                  Vínculos Omnichannel (Opcionais)
                </p>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                  <div>
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1 flex items-center gap-1">
                      <Send className="w-3 h-3 text-sky-600" />
                      Telegram Chat ID
                    </label>
                    <input
                      type="text"
                      value={formTelegramChatId}
                      onChange={(e) => setFormTelegramChatId(e.target.value)}
                      placeholder="Ex: 987654321"
                      className="w-full px-2.5 py-1.5 text-xs rounded-lg border border-slate-200 focus:outline-none focus:border-teal-500 font-mono bg-white"
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1 flex items-center gap-1">
                      <MessageCircle className="w-3 h-3 text-emerald-600" />
                      WhatsApp (Número)
                    </label>
                    <input
                      type="text"
                      value={formWhatsappNumber}
                      onChange={(e) => setFormWhatsappNumber(e.target.value)}
                      placeholder="Ex: 5511988887777"
                      className="w-full px-2.5 py-1.5 text-xs rounded-lg border border-slate-200 focus:outline-none focus:border-teal-500 font-mono bg-white"
                    />
                  </div>
                </div>
              </div>

              {/* Modal Footer */}
              <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={fecharModal}
                  className="px-4 py-2 rounded-xl border border-slate-200 text-xs font-semibold text-slate-600 hover:bg-slate-50 cursor-pointer"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={salvando}
                  className="px-5 py-2 rounded-xl bg-teal-600 hover:bg-teal-700 text-white text-xs font-semibold shadow-sm cursor-pointer disabled:opacity-50 transition-colors"
                >
                  {salvando ? 'Salvando...' : pacienteEditando ? 'Salvar Alterações' : 'Cadastrar Paciente'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
