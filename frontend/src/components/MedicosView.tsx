import React, { useState, useEffect } from 'react';
import { Stethoscope, Mail, Phone, Award, CheckCircle } from 'lucide-react';
import type { Medico } from '../types';
import { api } from '../services/api';

export const MedicosView: React.FC = () => {
  const [medicos, setMedicos] = useState<Medico[]>([]);

  useEffect(() => {
    api.getMedicos().then(setMedicos).catch(console.error);
  }, []);

  return (
    <div className="p-8 space-y-6 max-w-7xl mx-auto">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Corpo Clínico & Especialidades</h2>
        <p className="text-sm text-slate-500 mt-1">
          Profissionais cadastrados no sistema e habilitados para agendamento automático no bot.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {medicos.map((m) => (
          <div key={m.id} className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-shadow">
            <div className="flex items-start justify-between">
              <div className="w-12 h-12 rounded-xl bg-teal-50 text-teal-700 flex items-center justify-center font-bold text-lg">
                <Stethoscope className="w-6 h-6" />
              </div>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 flex items-center gap-1">
                <CheckCircle className="w-3 h-3" />
                Ativo
              </span>
            </div>

            <div className="mt-4">
              <h3 className="font-bold text-slate-900 text-base">{m.nome}</h3>
              <p className="text-xs font-semibold text-teal-700 mt-0.5">{m.especialidade.nome}</p>
              <p className="text-xs text-slate-400 mt-1">{m.especialidade.descricao}</p>
            </div>

            <div className="mt-4 pt-4 border-t border-slate-100 space-y-2 text-xs text-slate-600">
              <div className="flex items-center gap-2">
                <Award className="w-4 h-4 text-slate-400" />
                <span className="font-mono">{m.crm}</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-slate-400" />
                <span>{m.email}</span>
              </div>
              <div className="flex items-center gap-2">
                <Phone className="w-4 h-4 text-slate-400" />
                <span>{m.telefone}</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
