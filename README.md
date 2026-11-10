# OmniMed Care — Plataforma Omnichannel de Atendimento Médico

> **Solução Enterprise de Atendimento e Orquestração em Saúde**  
> Centralização de canais de mensageria (**Telegram**, **WhatsApp**, **WebChat**), motor transacional em **Java 21 / Spring Boot 3**, persistência relacional com JPA e interface administrativa para recepção médica em **React 18 + TypeScript + Tailwind CSS**.

---

## 🌟 Visão Geral da Arquitetura

```
medical-crm/
├── backend/                  # API REST, Persistência JPA e Motor Conversacional
│   ├── src/main/java/com/omnimed/crm/
│   │   ├── config/           # Configurações de CORS e Segurança
│   │   ├── controller/       # Endpoints REST (Agendamentos, Pacientes, Médicos, Live Chat, WhatsApp Webhook, Simulador)
│   │   ├── dto/              # Objetos de Transferência de Dados (Records Java 21)
│   │   ├── model/            # Entidades JPA (Paciente, Médico, Agendamento, Sessão, Mensagem, Exame)
│   │   ├── repository/       # Repositórios Spring Data JPA
│   │   └── service/          # Regras de Negócio, Safety Boundary, Timeout Automático e Bots (Telegram & WhatsApp)
│   └── src/main/resources/
│       └── application.yml   # Configuração H2/PostgreSQL, Telegram, WhatsApp e Timeout de Inatividade
│
└── frontend/                 # Painel Web do Operador & Live Chat da Recepção
    ├── src/
    │   ├── components/       # Dashboard, Inbox Live Chat, Agendamentos (com Filtros), Pacientes (CRUD), Corpo Clínico (CRUD), Simulador Omnichannel
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
3. Ao rodar o backend, o sistema ativará o **Long Polling automático**, processando conversas em tempo real direto do Telegram!

---

### 3. Subindo o Gateway WhatsApp Gratuito (Evolution API via Docker)

O projeto já inclui um `docker-compose.yml` completo pronto para uso.

1. **Inicie o Gateway WhatsApp via Docker:**
   * Dê um duplo clique no script **`iniciar_whatsapp_gateway.bat`**, ou execute no terminal:
     ```bash
     docker-compose up -d
     ```
   * O container da **Evolution API** iniciará na porta **`http://localhost:8081`** com banco PostgreSQL dedicado para persistência da sessão.

2. **Conecte seu WhatsApp sem precisar de Postman:**
   * Abra a interface web em `http://localhost:5173` e vá até a aba **Simulador / Bot Demo**.
   * No card lateral **"Gateway WhatsApp (Docker)"**, clique em **"Conectar / Ler QR Code"**.
   * O sistema criará a instância automaticamente, configurará o webhook e exibirá o **QR Code** na tela.
   * Abra o WhatsApp no seu celular ➡️ **Aparelhos Conectados** ➡️ **Conectar Aparelho** e leia o QR Code!

Pronto! Qualquer mensagem enviada para o seu número será atendida e processada pelo motor conversacional do OmniMed Care.

---

### 4. Executando o Frontend (React + Vite)

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
   - Centraliza conversas do Telegram, WhatsApp e WebChat.
   - Permite à recepcionista visualizar o histórico, clicar em **"Assumir Atendimento"**, responder diretamente ao paciente em seu canal de origem e **"Devolver ao Bot"** ou finalizar após solucionar a demanda.
3. **📅 Grade de Agendamentos Médicos (com Filtros Avançados):**
   - Lista todas as consultas marcadas pelos pacientes via bot com protocolo único (`#OM-XXXXX`).
   - **Filtros completos:** por médico, especialidade médica, dia/data específica (com atalho para "Hoje"), status da consulta e canal de origem (Telegram, WhatsApp, Web).
   - Cancelamento que automaticamente libera o horário na grade médica.
4. **👥 Gestão de Pacientes (CRUD Completo & Cadastro via Bot):**
   - Listagem com pesquisa em tempo real por nome, CPF, telefone ou e-mail.
   - Cadastro, edição e exclusão de pacientes pelo painel web.
   - **Cadastro Guiado no Chatbot:** Pacientes não identificados são convidados pelo bot a realizar o cadastro diretamente na conversa (coletando Nome, Telefone e E-mail), ficando aptos a agendar consultas instantaneamente.
5. **👨‍⚕️ Corpo Clínico & Especialidades (CRUD Completo):**
   - Gestão de profissionais: cadastro de novos médicos, edição de dados (CRM, especialidade, e-mail, telefone) e controle de status (ativo/inativo).
   - Ao cadastrar um médico, o sistema gera automaticamente slots de atendimento para agendamento imediato no bot.
6. **⏳ Encerramento Amigável & Timeout por Inatividade:**
   - **Encerramento pelo Cliente:** O paciente pode encerrar a conversa a qualquer momento digitando `sair`, `encerrar`, `fim` ou selecionando a opção **6** no menu principal.
   - **Timeout Automático:** Sessões inativas por mais de 10 minutos (configurável em `omnimed.chat.inactivity-timeout-minutes`) são automaticamente finalizadas com aviso amigável, liberando recursos e mantendo a segurança dos dados.
7. **📱 Simulador Interativo Omnichannel:**
   - Celular interativo para demonstrar os fluxos conversacionais nos modos **WhatsApp**, **Telegram** e **Web Chat**.

---

## 🛡️ Conformidade e Limite Ético-Médico (*Safety Boundary*)

- **Não Realização de Diagnóstico:** O assistente virtual atua estritamente em serviços administrativos, agendamentos, orientações e triagem.
- **Detecção de Emergências:** Palavras-chave associadas a quadros agudos (infarto, dor intensa no peito, falta de ar severa) acionam imediatamente a diretriz de emergência, orientando o paciente a acionar o SAMU (192) ou comparecer ao pronto-socorro.
- **LGPD:** Isolamento de papéis, dados sensíveis protegidos e auditoria de mensagens.
