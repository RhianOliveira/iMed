# iMed - Sistema de Gestão Médica

Este projeto é um aplicativo Android desenvolvido para a disciplina de **Tópicos Especiais em Informática** na **FATEC São Caetano do Sul**. O **iMed** é uma plataforma completa para intermediação e gestão de consultas médicas, focada em uma experiência de usuário (UX) intuitiva e um design moderno.

---

## 🚀 Visão Geral
O iMed foi concebido para digitalizar o fluxo de atendimento clínico, integrando três perfis distintos de usuários (Administrador, Médico e Paciente) em um ecossistema unificado, utilizando tecnologias de ponta em nuvem.

## 🛠 Tecnologias Utilizadas
- **Linguagem:** [Kotlin](https://kotlinlang.org/) (Nativo)
- **Banco de Dados:** [Firebase Firestore](https://firebase.google.com/docs/firestore) (NoSQL em tempo real)
- **Autenticação:** [Firebase Auth](https://firebase.google.com/docs/auth)
- **Gráficos e Dashboards:** [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- **Carregamento de Imagens:** [Coil](https://coil-kt.github.io/coil/) (com transformações circulares)
- **Interface:** Material Design 3 (Componentes modernos e responsivos)

---

## 📱 Funcionalidades Principais

### 👤 Perfil Paciente
- **Cadastro e Autenticação:** Fluxo seguro de criação de conta e recuperação de senha por e-mail.
- **Gestão de Dados:** Edição de dados pessoais, saúde (tipo sanguíneo, alergias) e endereço com máscaras automáticas (CPF, CEP, Telefone).
- **Agendamento Inteligente:** Filtro de médicos por especialidade e seleção de horários disponíveis em tempo real.
- **Histórico Clínico:** Acesso fácil a todas as consultas passadas e visualização detalhada de prontuários liberados pelos médicos.

### 👨‍⚕️ Perfil Médico
- **Agenda Dinâmica:** Visualização de consultas do dia com controle de presença e cancelamento (regra de negócio de 24h de antecedência).
- **Prontuário Eletrônico:** Processo de atendimento dividido em etapas (Anamnese e Conclusão Clínica) para maior organização.
- **Histórico de Atendimentos:** Busca rápida de pacientes atendidos com ordenação cronológica decrescente.

### 🔑 Perfil Administrador (Gestão)
- **Controle de Corpo Clínico:** Cadastro e edição de médicos, incluindo gestão de fotos.
- **Especialidades Dinâmicas:** Sistema flexível para adicionar novas especialidades médicas que ficam disponíveis instantaneamente para todo o app.
- **Dashboard de Relatórios:** Visão analítica com:
  - Total de consultas, cancelamentos e faltas.
  - Gráfico de pizza com as especialidades mais procuradas (Top 9 + Outros).
  - Gráfico de barras com a evolução mensal de atendimentos realizados.

---

## 🎨 Design e UX
O projeto destaca-se pela sua identidade visual consistente e elegante:
- **Identidade Visual:** Uso da cor `primary_navy` (#06152D) para autoridade e `pure_white` para limpeza visual.
- **Cards Arredondados:** Layout baseado em cartões com curvaturas customizadas (`LoginCard` style), proporcionando um aspecto moderno.
- **Inputs Modernos:** Campos de entrada sem rótulos externos, utilizando *hints* internos e foco destacado para economizar espaço e reduzir ruído visual.
- **Responsividade:** Interface otimizada para diferentes tamanhos de tela, com ajustes específicos para dispositivos de alta densidade como o Galaxy S23.

---

## 🏗 Estrutura do Projeto
O código segue padrões de desenvolvimento Android recomendados, com separação clara de responsabilidades:
- **Activities:** Gestão de ciclo de vida e interação com o usuário.
- **Adapters:** Lógica de exibição de listas dinâmicas (RecyclerViews).
- **MaskHelper:** Utilitário centralizado para formatação de dados em tempo real.
- **Firestore Integration:** Camada de serviço para comunicação assíncrona com o banco de dados.

---
