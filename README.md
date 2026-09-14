# 🏥 GESTMED - Sistema Hospitalar de Agendamentos e Notificações (Tech Challenge - Fase 3)

## 📝 Descrição do Projeto
Este projeto consiste no desenvolvimento de um backend simplificado, modular e escalável voltado para o ambiente hospitalar. A solução gerencia o agendamento de consultas, histórico de pacientes e envio assíncrono de lembretes automáticos via mensageria.

---

## 🏗️ Arquitetura do Sistema

A solução foi estruturada no modelo de **Microserviços** (ou módulos separados) para garantir o desacoplamento e a escalabilidade da aplicação:

1. **Service de Agendamento (`scheduling-service`)**:
   - Responsável pelo gerenciamento (Criação/Edição) de consultas médicas.
   - Publica eventos de agendamento na fila para o serviço de notificação.
2. **Service de Notificações (`notification-service`)**:
   - Consome as mensagens enviadas via mensageria.
   - Simula/executa o envio de lembretes aos pacientes para consultas agendadas ou alteradas.
3. **Service de Histórico (`history-service`)**:
   - Disponibiliza a interface **GraphQL** para consultas flexíveis sobre o histórico médico.

### 🔄 Fluxo de Comunicação Assíncrona
- **Ferramenta de Mensageria:** RabbitMQ
- Ao criar ou alterar uma consulta no *Serviço de Agendamento*, uma mensagem/evento é publicado na fila/tópico `consultas-agendadas`.
- O *Serviço de Notificações* escuta a fila/tópico e processa o envio do lembrete ao paciente de forma não bloqueante.

---

## 🔒 Segurança e Autenticação (Spring Security)

A aplicação utiliza **Spring Security** para controle de acesso baseado em papéis (RBAC - *Role-Based Access Control*):

| Perfil (`Role`) | Permissões |
| :--- | :--- |
| **`ROLE_DOCTOR`** | Visualizar e editar histórico de consultas; alterar agendamentos. |
| **`ROLE_NURSE`** | Registrar novas consultas e acessar histórico de pacientes. |
| **`ROLE_PATIENT`** | Visualizar apenas suas próprias consultas agendadas. |

*O token Basic Auth deve ser repassado no header `Authorization` de cada requisição.*

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java 25
- **Framework Principal:** Spring Boot 3.x (Spring Security, Spring Data JPA)
- **API Query Language:** GraphQL
- **Mensageria:** RabbitMQ
- **Banco de Dados:** MySQL / MYSQL
- **Containerização:** Docker & Docker Compose
- **Testes de API:** Postman Collection

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Docker e Docker Compose instalados.
- Java 25.

### Passos para Execução

1. **Clonar o repositório:**
   bash
   git clone [https://github.com/jeniblodev/gestmed]
   cd gestmed
2. **Subir o serviço via Docker Compose:**
   bash
   docker-compose up -d --build
3. **Verifique se os serviços estão operacionais:**
   Serviço de Agendamento: http://localhost:8081
   Serviço de GraphQL/Histórico: http://localhost:8082/graphql
   Painel RabbitMQ (se aplicável): http://localhost:15672 (guest/guest)
