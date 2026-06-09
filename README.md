# 🎪 Plataforma Transacional de Mercados Municipais (Enterprise PoC)

> **Solução distribuída baseada em Microsserviços e Arquitetura Orientada a Eventos (EDA) para a modernização e digitalização regulamentar de feiras municipais.**
> Alinhado com o Regime Jurídico das Atividades de Comércio, Serviços e Restauração (**RJACSR - Decreto-Lei n.º 10/2015**).

---

## 🏗️ 1. Visão Geral da Arquitetura

A solução adota os princípios de **Domain-Driven Design (DDD)** e isolamento de contextos (*Bounded Contexts*), garantindo tolerância a falhas, escalabilidade linear e independência de deployments através de bases de dados 100% segregadas.

```
                  ┌────────────────────────────────────────┐
                  │          Frontend (Angular UI)         │
                  └───────────────────┬────────────────────┘
                                      │ HTTP (Port 4200)
                                      ▼
                  ┌────────────────────────────────────────┐
                  │       API Gateway & Identidade         │ (JWT / RBAC)
                  └───────────────────┬────────────────────┘
                                      │
         ┌────────────────────────────┼────────────────────────────┐
         ▼ HTTP                       ▼ HTTP                       ▼ HTTP
┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐
│Municipios-Service│         │ Mercados-Service │         │ Cidadaos-Service │
│   (Port 8081)    │         │   (Port 8082)    │         │   (Port 8083)    │
└────────┬─────────┘         └────────┬─────────┘         └────────┬─────────┘
         │                            │                            │
         └──────────────┬─────────────┴─────────────┬──────────────┘
                        │                           │
                        ▼ Eventos                   ▼ Eventos
           ┌─────────────────────────┐ ┌─────────────────────────┐
           │      Apache Kafka       │ │  Notificacao-Service    │ (Port 8084)
           │      (Event Broker)     │ │  (Consumidor Assíncrono)│
           └─────────────────────────┘ └────────────┬────────────┘
                                                    │ SMTP (Port 1025)
                                                    ▼
                                       ┌─────────────────────────┐
                                       │     Mailhog Server      │ (Port 8025 UI)
                                       └─────────────────────────┘
```

### 🛠️ Stack Tecnológica Central
* **Frontend:** Angular 17+ (Componentes Standalone, Lógica Reativa com RxJS e Signals para controlo de estado).
* **Backend:** Java 21, Spring Boot 3.x, Spring Security (Autenticação Stateless com tokens JWT assinados).
* **Mensageria:** Apache Kafka (Garante consistência eventual de dados e sincronização de tabelas espelho CQRS).
* **Persistência:** PostgreSQL (Instâncias e volumes isolados por microsserviço).
* **Mock transacional:** Mailhog (Servidor SMTP local para interceção de e-mails institucionais).

---

## 🚦 2. Matriz de Serviços e Portas

| Componente / Microsserviço | Porta Local | Base de Dados / Dependência | Descrição Core |
| :--- | :--- | :--- | :--- |
| **`frontend-ui`** | `4200` | Nginx / Angular UI | Interface do Cidadão, Feirante e Balcão da Autarquia. |
| **`identidade-service`** | `8080` | `db_identidade` (5432) | API Gateway e provedor Auth JWT. Filtra e propaga o Role via Header. |
| **`municipios-service`** | `8081` | `db_municipios` (5433) | Registo institucional de Câmaras Municipais e Juntas. |
| **`mercados-service`** | `8082` | `db_mercados` (5434) | Motor de Feiras, Vagas, Preçários Regulamentares e Candidaturas. |
| **`cidadaos-service`** | `8083` | `db_cidadaos` (5435) | Gestão do perfil e histórico do utilizador comum. |
| **`notificacao-service`** | `8084` | *Nenhuma (Reactive)* | Consumidor Kafka. Dispara e-mails HTML transacionais. |
| **`kafka`** | `9092` | KRaft Cluster Mode | Broker de mensageria assíncrona entre os ecossistemas. |
| **`mailhog`** | `8025` | SMTP Engine (1025) | Interface Web de visualização de e-mails em tempo real. |

---

## 🚀 3. Como Executar (Quick Start)

### Pré-requisitos
* Ter o **Docker** e o **Docker Compose** instalados na máquina.
* Garantir que as portas mapeadas na matriz acima estão totalmente livres.

### Passo Único de Inicialização
Navega até à raiz do repositório (onde se encontra o ficheiro `docker-compose.yml`) e executa o seguinte comando no teu terminal:

```bash
docker compose up -d --build
```

O Docker irá descarregar as imagens oficiais, compilar o código fonte Java e Angular, estruturar as redes isoladas em ponte (`mercadinhos-net`) e levantar todo o ecossistema em pano de fundo.

---

## 🎯 4. Links de Acesso Local

Após o arranque bem-sucedido de todos os contentores, podes aceder aos seguintes pontos de entrada diretamente no teu browser:

* 💻 **Portal Web (Frontend Angular):** [http://localhost:4200](http://localhost:4200)
* 📬 **Caixa de E-mails (Mailhog Web UI):** [http://localhost:8025](http://localhost:8025)
* 🔒 **Healthcheck Gateway (API Identidade):** `http://localhost:8080/actuator/health`

---

## 🎪 5. Roteiro Prático para Demonstração (Live Demo de 5 Minutos)

Para provar o "Fator Uau" técnico e de negócio aos stakeholders, segue este fluxo na interface:

1. **O Registo e o Alerta Reativo:** Acede ao Portal (`:4200`), efetua o registo de uma nova conta de Feirante. Abre imediatamente o painel do Mailhog (`:8025`) e verás o e-mail de boas-vindas institucional formatado em HTML a cair em tempo real.
2. **A Publicação do Regulamento Técnico:** Autentica-te como utilizador do Município. Cria uma nova feira regulamentar. Repara no autocomplete geográfico a funcionar via API Photon e define os preçários diários ou por evento para stands e roulottes de street food. Adiciona uma descrição personalizada e o link para o cartaz do evento.
3. **A Vitrine do Cidadão:** Faz logout e entra como cidadão comum. Abre o mapa interativo ou a lista de feiras públicas. Ao clicares na feira que criaste, verás o modal renderizar dinamicamente o cartaz configurado, a descrição adaptada e os selos reativos de comodidade (**Selo Pet Friendly** e **Infraestrutura Sanitária/WCs** públicos).
4. **Resiliência de Logs:** No terminal, corre `docker logs -f notificacao-service` para ver a magia da arquitetura orientada a eventos, detalhando o consumo das mensagens cruas do Kafka e o tratamento seguro contra *Poison Pills*.

---
*Desenvolvido sob os padrões de excelência em engenharia e arquitetura da Devoteam Portugal.*
---