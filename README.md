# 🏥 GESTMED - Sistema Hospitalar de Agendamentos e Notificações (Tech Challenge - Fase 3)

## 📝 Descrição do Projeto
Este projeto consiste no desenvolvimento de um backend simplificado, modular e escalável voltado para o ambiente hospitalar. A solução gerencia o agendamento de consultas, histórico de pacientes e envio assíncrono de lembretes automáticos via mensageria.

---

## 🏗️ Arquitetura do Sistema

A solução foi estruturada no modelo de **Microserviços** (ou módulos separados) para garantir o desacoplamento e a escalabilidade da aplicação:

1. **Service de Agendamento (`scheduling-service`)**:
   - Responsável pelo gerenciamento (Criação/Edição) de consultas médicas.
   - Publica eventos de criação, atualização e cancelamento de consultas no RabbitMQ.
2. **Service de Notificações (`notification-service`)**:
   - Consome as mensagens enviadas via mensageria.
   - Simula/executa o envio de lembretes aos pacientes para consultas agendadas ou alteradas.
3. **Service de Histórico (`history-service`)**:
   - Consome os eventos de consulta, mantém todas as alterações históricas e disponibiliza uma interface **GraphQL** para consulta.

### 🔄 Fluxo de Comunicação Assíncrona
- **Ferramenta de Mensageria:** RabbitMQ
- O Scheduling publica no tópico `hospital.exchange` usando as chaves `appointment.created`, `appointment.updated` e `appointment.cancelled`.
- History e Notification possuem filas independentes, permitindo que os dois serviços processem o mesmo evento sem acoplamento.
- As filas possuem filas de mensagens mortas (DLQ) para eventos que não puderem ser processados após as tentativas configuradas.

---

## 🔒 Segurança e Autenticação (Spring Security)

A aplicação utiliza **Spring Security** para controle de acesso baseado em papéis (RBAC - *Role-Based Access Control*):

| Perfil (`Role`) | Permissões |
| :--- | :--- |
| **`ROLE_DOCTOR`** | Criar e alterar agendamentos; consultar todos os agendamentos e históricos. |
| **`ROLE_NURSE`** | Criar e alterar agendamentos; consultar todos os agendamentos e históricos. |
| **`ROLE_PATIENT`** | Visualizar apenas suas próprias consultas e seu próprio histórico. |

*O token Basic Auth deve ser repassado no header `Authorization` de cada requisição.*

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java 21
- **Framework Principal:** Spring Boot 4.1.1 (Spring Security, Spring Data JPA)
- **API Query Language:** GraphQL
- **Mensageria:** RabbitMQ
- **Banco de Dados:** MySQL / MYSQL
- **Containerização:** Docker & Docker Compose
- **Testes de API:** Postman Collection

---

## 🚀 Como executar localmente

A forma recomendada é executar toda a aplicação com Docker Compose. Nesse modo, não é necessário instalar Java, Maven, MySQL ou RabbitMQ na máquina: as imagens Docker fazem o build dos três serviços e fornecem toda a infraestrutura.

### Pré-requisitos

- Git;
- Docker Engine ou Docker Desktop em execução;
- Docker Compose v2 (comando `docker compose`);
- portas `3308`, `5672`, `8081`, `8083` e `15672` livres.

Confira a instalação:

```bash
git --version
docker --version
docker compose version
```

### 1. Clonar o repositório

Usando HTTPS:

```bash
git clone https://github.com/jeniblodev/gestmed.git
cd gestmed
```

Ou, se já tiver uma chave SSH no configurada no GitHub:

```bash
git clone git@github.com:jeniblodev/gestmed.git
cd gestmed
```

### 2. Construir e iniciar a aplicação

Execute o comando na raiz do repositório, onde está o arquivo `docker-compose.yml`:

```bash
docker compose up -d --build
```

Na primeira execução, o Docker baixa as imagens e dependências Maven, portanto o processo pode demorar alguns minutos. O Compose inicia:

| Componente | Container | Acesso local |
| :--- | :--- | :--- |
| Scheduling | `gestmed-scheduling` | `http://localhost:8081/graphql` |
| History | `gestmed-history` | `http://localhost:8083/graphql` |
| Notification | `gestmed-notification` | sem porta HTTP; acompanhamento pelos logs |
| MySQL | `gestmed-mysql` | `localhost:3308` |
| RabbitMQ | `gestmed-rabbitmq` | `localhost:5672` |
| RabbitMQ Management | `gestmed-rabbitmq` | `http://localhost:15672` |

O MySQL cria automaticamente os bancos `scheduling` e `history`. As migrações são aplicadas pelo Flyway quando os serviços iniciam.

### 3. Verificar a inicialização

Confira o estado dos containers:

```bash
docker compose ps
```

Os cinco containers devem aparecer como `Up`/`running`; MySQL e RabbitMQ também devem ficar `healthy`. Para acompanhar a inicialização de todos os componentes:

```bash
docker compose logs -f
```

Para sair da visualização dos logs sem desligar os containers, utilize `Ctrl+C`.

Também é possível acompanhar cada serviço separadamente:

```bash
docker compose logs -f scheduling
docker compose logs -f history
docker compose logs -f notification
```

O serviço de notificação não envia e-mail real. Conforme o escopo do projeto, o recebimento e a simulação do envio são registrados no log do container `notification`.

### 4. Acessos e credenciais locais

As requisições GraphQL usam Basic Auth. O ambiente Docker cria estes usuários de demonstração:

| Perfil | Usuário | Senha |
| :--- | :--- | :--- |
| Médico | `doctor` | `doctor123` |
| Enfermeiro | `nurse` | `nurse123` |
| Paciente | `patient` | `patient123` |

Endpoints:

- Scheduling GraphQL: `POST http://localhost:8081/graphql`
- Scheduling GraphiQL: `http://localhost:8081/graphiql`
- History GraphQL: `POST http://localhost:8083/graphql`
- History GraphiQL: `http://localhost:8083/graphiql`
- RabbitMQ Management: `http://localhost:15672` (`guest` / `guest`)

Ao acessar o GraphiQL pelo navegador, informe uma das credenciais Basic Auth acima quando solicitado. No Postman, selecione **Authorization > Basic Auth** e preencha usuário e senha.

### 5. Encerrar a aplicação

Para parar e remover apenas os containers e a rede, preservando os dados locais:

```bash
docker compose down
```

Para iniciar novamente sem refazer as imagens:

```bash
docker compose up -d
```

