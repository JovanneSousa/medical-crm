import React, { useState, useEffect, useMemo } from 'react';
import { 
  Stethoscope, 
  Mail, 
  Phone, 
  Award, 
  CheckCircle, 
  XCircle, 
  Plus, 
  Edit3, 
  Trash2, 
  Search, 
  X,
  AlertCircle,
  RotateCcw
} from 'lucide-react';
import type { Especialidade, Medico, MedicoRequest } from '../types';
import { api } from '../services/api';

export const MedicosView: React.FC = () => {
  const [medicos, setMedicos] = useState<Medico[]>([]);
  const [especialidades, setEspecialidades] = useState<Especialidade[]>([]);
  const [carregando, setCarregando] = useState(false);

  // Filtros
  const [busca, setBusca] = useState('');
  const [filtroEspecialidade, setFiltroEspecialidade] = useState('');
  const [filtroStatus, setFiltroStatus] = useState<string>('TODOS');

  // Modal de Criação / Edição
  const [modalAberto, setModalAberto] = useState(false);
  const [medicoEditando, setMedicoEditando] = useState<Medico | null>(null);
  const [formErro, setFormErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  // Campos do Formulário
  const [formNome, setFormNome] = useState('');
  const [formCrm, setFormCrm] = useState('');
  const [formEspecialidadeId, setFormEspecialidadeId] = useState<number | ''>('');
  const [formEmail, setFormEmail] = useState('');
  const [formTelefone, setFormTelefone] = useState('');
  const [formAtivo, setFormAtivo] = useState(true);

  const carregarDados = async () => {
    try {
      setCarregando(true);
      const [listaMedicos, listaEsp] = await Promise.all([
        api.getMedicos(),
        api.getEspecialidades(),
      ]);
      setMedicos(listaMedicos);
      setEspecialidades(listaEsp);
    } catch (err) {
      console.error('Falha ao carregar corpo clínico:', err);
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    carregarDados();
  }, []);

  const abrirModalNovo = () => {
    setMedicoEditando(null);
    setFormNome('');
    setFormCrm('');
    setFormEspecialidadeId(especialidades.length > 0 ? especialidades[0].id : '');
    setFormEmail('');
    setFormTelefone('');
    setFormAtivo(true);
    setFormErro(null);
    setModalAberto(true);
  };

  const abrirModalEditar = (m: Medico) => {
    setMedicoEditando(m);
    setFormNome(m.nome);
    setFormCrm(m.crm);
    setFormEspecialidadeId(m.especialidade.id);
    setFormEmail(m.email || '');
    setFormTelefone(m.telefone || '');
    setFormAtivo(m.ativo);
    setFormErro(null);
    setModalAberto(true);
  };

  const fecharModal = () => {
    setModalAberto(false);
    setMedicoEditando(null);
    setFormErro(null);
  };

  const handleSalvar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formNome.trim() || !formCrm.trim() || !formEspecialidadeId) {
      setFormErro('Por favor, preencha todos os campos obrigatórios (Nome, CRM e Especialidade).');
      return;
    }

    try {
      setSalvando(true);
      setFormErro(null);

      const payload: MedicoRequest = {
        nome: formNome.trim(),
        crm: formCrm.trim(),
        especialidadeId: Number(formEspecialidadeId),
        email: formEmail.trim(),
        telefone: formTelefone.trim(),
        ativo: formAtivo,
      };

      if (medicoEditando) {
        await api.updateMedico(medicoEditando.id, payload);
      } else {
        await api.createMedico(payload);
      }

      fecharModal();
      await carregarDados();
    } catch (err: any) {
      setFormErro(err.message || 'Erro ao salvar profissional.');
    } finally {
      setSalvando(false);
    }
  };

  const handleExcluir = async (m: Medico) => {
    if (!confirm(`Deseja realmente remover ou inativar o Dr(a). ${m.nome}?`)) return;

    try {
      const res = await api.deleteMedico(m.id);
      if (res?.mensagem) {
        alert(res.mensagem);
      }
      await carregarDados();
    } catch (err: any) {
      alert(err.message || 'Erro ao excluir médico.');
    }
  };

  // Filtragem local
  const medicosFiltrados = useMemo(() => {
    return medicos.filter((m) => {
      if (busca.trim()) {
        const termo = busca.toLowerCase().trim();
        const bateNome = m.nome.toLowerCase().includes(termo);
        const bateCrm = m.crm.toLowerCase().includes(termo);
        if (!bateNome && !bateCrm) return false;
      }

      if (filtroEspecialidade && m.especialidade.nome !== filtroEspecialidade) {
        return false;
      }

      if (filtroStatus === 'ATIVOS' && !m.ativo) return false;
      if (filtroStatus === 'INATIVOS' && m.ativo) return false;

      return true;
    });
  }, [medicos, busca, filtroEspecialidade, filtroStatus]);

  return (
    <div className="p-8 space-y-6 max-w-7xl mx-auto">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Corpo Clínico & Especialidades</h2>
          <p className="text-sm text-slate-500 mt-1">
            Gestão completa de médicos cadastrados, especialidades clínicas e disponibilidade no assistente virtual.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={carregarDados}
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
            Novo Médico
          </button>
        </div>
      </div>

      {/* Barra de Filtros */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col md:flex-row items-center gap-3">
        <div className="relative flex-1 w-full">
          <Search className="w-4 h-4 absolute left-3 top-3 text-slate-400" />
          <input
            type="text"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Pesquisar por médico ou CRM..."
            className="w-full pl-9 pr-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500"
          />
        </div>

        <div className="w-full md:w-56">
          <select
            value={filtroEspecialidade}
            onChange={(e) => setFiltroEspecialidade(e.target.value)}
            className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500 bg-white"
          >
            <option value="">Todas Especialidades</option>
            {especialidades.map((esp) => (
              <option key={esp.id} value={esp.nome}>{esp.nome}</option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-1.5 w-full md:w-auto">
          {['TODOS', 'ATIVOS', 'INATIVOS'].map((s) => (
            <button
              key={s}
              onClick={() => setFiltroStatus(s)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold cursor-pointer transition-all ${
                filtroStatus === s
                  ? 'bg-slate-900 text-white shadow-sm'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {s === 'TODOS' ? 'Todos' : s === 'ATIVOS' ? 'Ativos' : 'Inativos'}
            </button>
          ))}
        </div>
      </div>

      {/* Grid de Cards dos Médicos */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {medicosFiltrados.length === 0 ? (
          <div className="col-span-full bg-white p-12 rounded-2xl border border-slate-200 text-center text-slate-400">
            <Stethoscope className="w-10 h-10 mx-auto text-slate-300 mb-2" />
            <p className="font-semibold text-slate-700">Nenhum profissional encontrado.</p>
            <p className="text-xs text-slate-400 mt-1">Clique em "Novo Médico" para cadastrar um novo integrante.</p>
          </div>
        ) : (
          medicosFiltrados.map((m) => (
            <div
              key={m.id}
              className={`bg-white p-6 rounded-2xl border transition-all ${
                m.ativo ? 'border-slate-200/80 shadow-sm hover:shadow-md' : 'border-slate-200 bg-slate-50/60 opacity-80'
              }`}
            >
              <div className="flex items-start justify-between">
                <div className={`w-12 h-12 rounded-xl flex items-center justify-center font-bold text-lg ${
                  m.ativo ? 'bg-teal-50 text-teal-700' : 'bg-slate-100 text-slate-400'
                }`}>
                  <Stethoscope className="w-6 h-6" />
                </div>
                <div className="flex items-center gap-2">
                  <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold flex items-center gap-1 ${
                    m.ativo ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'
                  }`}>
                    {m.ativo ? <CheckCircle className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
                    {m.ativo ? 'Ativo' : 'Inativo'}
                  </span>
                </div>
              </div>

              <div className="mt-4">
                <h3 className="font-bold text-slate-900 text-base">{m.nome}</h3>
                <p className="text-xs font-semibold text-teal-700 mt-0.5">{m.especialidade.nome}</p>
                <p className="text-xs text-slate-400 mt-1 line-clamp-2">{m.especialidade.descricao}</p>
              </div>

              <div className="mt-4 pt-4 border-t border-slate-100 space-y-2 text-xs text-slate-600">
                <div className="flex items-center gap-2">
                  <Award className="w-4 h-4 text-slate-400 shrink-0" />
                  <span className="font-mono font-medium">{m.crm}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Mail className="w-4 h-4 text-slate-400 shrink-0" />
                  <span className="truncate">{m.email || 'Não informado'}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Phone className="w-4 h-4 text-slate-400 shrink-0" />
                  <span>{m.telefone || 'Não informado'}</span>
                </div>
              </div>

              {/* Botões de Ação do Card */}
              <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-end gap-2">
                <button
                  onClick={() => abrirModalEditar(m)}
                  className="px-3 py-1.5 rounded-lg border border-slate-200 text-slate-700 hover:bg-slate-50 text-xs font-semibold flex items-center gap-1.5 cursor-pointer transition-colors"
                >
                  <Edit3 className="w-3.5 h-3.5 text-slate-500" />
                  Editar
                </button>
                <button
                  onClick={() => handleExcluir(m)}
                  className="px-3 py-1.5 rounded-lg border border-rose-200 text-rose-700 hover:bg-rose-50 text-xs font-semibold flex items-center gap-1.5 cursor-pointer transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5 text-rose-500" />
                  Excluir
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Modal de Criação / Edição */}
      {modalAberto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
          <div className="bg-white w-full max-w-lg rounded-2xl shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
            {/* Modal Header */}
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <h3 className="font-bold text-slate-900 text-base flex items-center gap-2">
                <Stethoscope className="w-5 h-5 text-teal-600" />
                {medicoEditando ? 'Editar Médico' : 'Cadastrar Novo Médico'}
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
                  Nome Completo *
                </label>
                <input
                  type="text"
                  required
                  value={formNome}
                  onChange={(e) => setFormNome(e.target.value)}
                  placeholder="Ex: Dr. Roberto Alencar"
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    CRM *
                  </label>
                  <input
                    type="text"
                    required
                    value={formCrm}
                    onChange={(e) => setFormCrm(e.target.value)}
                    placeholder="Ex: CRM/SP 145982"
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500 font-mono"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Especialidade *
                  </label>
                  <select
                    required
                    value={formEspecialidadeId}
                    onChange={(e) => setFormEspecialidadeId(Number(e.target.value))}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500 bg-white"
                  >
                    <option value="">Selecione...</option>
                    {especialidades.map((esp) => (
                      <option key={esp.id} value={esp.id}>{esp.nome}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    E-mail Institucional
                  </label>
                  <input
                    type="email"
                    value={formEmail}
                    onChange={(e) => setFormEmail(e.target.value)}
                    placeholder="roberto.alencar@omnimed.com"
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Telefone / Contato
                  </label>
                  <input
                    type="text"
                    value={formTelefone}
                    onChange={(e) => setFormTelefone(e.target.value)}
                    placeholder="(11) 98111-2233"
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-200 focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>

              <div className="pt-2">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formAtivo}
                    onChange={(e) => setFormAtivo(e.target.checked)}
                    className="w-4 h-4 text-teal-600 rounded border-slate-300 focus:ring-teal-500"
                  />
                  <span className="text-xs font-semibold text-slate-800">
                    Médico Ativo (habilitado para agendamentos no Chatbot e recepção)
                  </span>
                </label>
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
                  {salvando ? 'Salvando...' : medicoEditando ? 'Salvar Alterações' : 'Cadastrar Médico'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
