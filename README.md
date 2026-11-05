# OmniMed Care — Plataforma Omnichannel de Atendimento Médico

> **Solução Enterprise de Atendimento e Orquestração em Saúde**  
> Centralização de canais de mensageria (Telegram, WebChat, WhatsApp), motor transacional em **Java 21 / Spring Boot 3**, persistência relacional com JPA e interface administrativa para recepção médica em **React 18 + TypeScript + Tailwind CSS**.

---

## 🌟 Visão Geral da Arquitetura

```
medical-crm/
├── backend/                  # API REST, Persistência JPA e Motor Conversacional
│   ├── src/main/java/com/omnimed/crm/
│   │   ├── config/           # Configurações de CORS e Segurança
│   │   ├── controller/       # Endpoints REST (Agendamentos, Live Chat, Médicos, Dashboard, Simulador)
│   │   ├── dto/              # Objetos de Transferência de Dados (Records Java 21)
│   │   ├── model/            # Entidades de Domínio JPA (Paciente, Médico, Agendamento, Sessão, Mensagem, Exame)
│   │   ├── repository/       # Repositórios Spring Data JPA
│   │   └── service/          # Regras de Negócio, Safety Boundary e Telegram Bot Engine
│   └── src/main/resources/
│       └── application.yml   # Configuração de Banco H2/PostgreSQL e Telegram Token
│
└── frontend/                 # Painel Web do Operador & Live Chat da Recepção
    ├── src/
    │   ├── components/       # Dashboard, Inbox Live Chat, Agendamentos, Corpo Clínico, Simulador
    │   ├── services/         # Cliente de API REST
    │   ├── types/            # Tipos e Interfaces TypeScript
    │   └── App.tsx           # Aplicação Principal com Navegação por Abas
    └── vite.config.ts        # Proxy reverso para backend Spring Boot (:8080)
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Java 21** ou superior
- **Apache Maven 3.9+**
- **Node.js 18+** e **npm**

---

### 1. Executando o Backend (Spring Boot)

No terminal:

```bash
cd backend
mvn spring-boot:run
```

O backend inicializará na porta **`http://localhost:8080`**.
- **Console do Banco H2:** `http://localhost:8080/h2-console`  
  - *JDBC URL:* `jdbc:h2:mem:omnimed_db` | *User:* `sa` | *Password:* *(vazio)*
- **Carga Inicial Automática:** O sistema já inicializa com especialidades (Cardiologia, Dermatologia, Ortopedia...), médicos credenciados, slots de horários nos próximos dias, pacientes e exames de demonstração.

---

### 2. Configurando o Telegram Bot (Opcional para Modo Real)

1. Crie seu bot no Telegram conversando com [@BotFather](https://t.me/BotFather) e obtenha seu token (`123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11`).
2. Abra `backend/src/main/resources/application.yml` ou defina as variáveis de ambiente:
   ```yaml
   omnimed:
     telegram:
       enabled: true
       bot-token: "SEU_TELEGRAM_BOT_TOKEN_AQUI"
       bot-username: "SeuBotUsername"
       polling-interval-ms: 2000
   ```
3. Ao rodar o backend, o sistema ativará o **Long Polling automático**, processando conversas em tempo real direto do aplicativo do Telegram dos pacientes!

---

### 3. Executando o Frontend (React + Vite)

Em outro terminal:

```bash
cd frontend
npm run dev
```

Acesse a interface no navegador: **`http://localhost:5173`**

---

## 💡 Principais Módulos do Sistema

1. **📊 Painel Executivo (Dashboard):**
   - KPIs de agendamentos, taxa de retenção do bot (> 85%), atendimentos ativos e fila de transbordo humano.
2. **💬 Inbox Omnichannel (Live Chat da Recepção):**
   - Centraliza conversas do Telegram e WebChat.
   - Permite à recepcionista visualizar o histórico, clicar em **"Assumir Atendimento"**, responder diretamente ao paciente no Telegram e **"Devolver ao Bot"** após solucionar a demanda.
3. **📅 Grade de Agendamentos Médicos:**
   - Lista todas as consultas marcadas pelos pacientes via bot com protocolo único (`#OM-XXXXX`), status e opção de cancelamento que automaticamente libera o horário de atendimento.
4. **👨‍⚕️ Corpo Clínico & Especialidades:**
   - Gestão de médicos ativos, CRM, contato e especialidades.
5. **📱 Simulador Interativo do Paciente:**
   - Widget integrado que simula um smartphone para demonstrar e testar todos os fluxos conversacionais (identificação de CPF, agendamento de consultas, consulta de exames com laudo, teste de transbordo humano e limites éticos de emergência médica).

---

## 🛡️ Conformidade e Limite Ético-Médico (*Safety Boundary*)

- **Não Realização de Diagnóstico:** O assistente virtual é programado para atuar estritamente em serviços administrativos, agendamentos, orientações e triagem.
- **Detecção de Emergências:** Palavras-chave associadas a quadros agudos (infarto, dor intensa no peito, falta de ar severa) acionam imediatamente a diretriz de emergência, orientando o paciente a acionar o SAMU (192) ou comparecer ao pronto-socorro.
- **LGPD:** Isolamento de papéis, dados sensíveis protegidos e auditoria de mensagens.
