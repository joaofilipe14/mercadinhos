# PoC: Sistema de Gestão de Mercados e Feirantes (Enterprise Edition)

## 🏢 Visão Geral da Arquitetura Atualizada
O sistema evoluiu para uma arquitetura profissional orientada a eventos e baseada em microsserviços isolados por contexto (*Bounded Context*), comunicando assincronamente.

* **`frontend`**: Aplicação SPA em **Angular** (Standalone & Signals), reativa e responsiva, exposta via Nginx (porta 4200).
* **`identidade-service` (Gateway)**: Ponto de entrada e central de segurança (RBAC, JWT). Orquestra as chamadas e o registo na porta `8080`.
* **`mercados-service`**: Gere as Feiras, Candidaturas, Pasta Digital de PDFs e espelho de Feirantes via Kafka (porta `8082`).
* **`municipios-service`**: Gere o perfil estendido das Câmaras e Juntas, também mantido via Kafka (porta `8083`).
* **`pagamentos-service` (NOVO - Mock)**: Simula aprovações financeiras para demonstrar transações distribuídas (porta `8084`).
* **`broker`**: Apache Kafka para Eventual Consistency e Sagas.

---

## 🎯 1. O Core do PoC (Requisitos Mínimos para a Demo)
*O "Caminho Feliz" que prova que a plataforma resolve o problema real de negócio.*

### ✅ Concluído (A não mexer!)
- [x] Infraestrutura base (`docker-compose.yml`, bases de dados PostgreSQL isoladas).
- [x] Gateway de API e Autenticação JWT centralizada.
- [x] Registo reativo focado em Roles (Frontend com UI Premium).
- [x] Sincronização de perfis assíncrona (Eventual Consistency) com Kafka (`feirante-registado-topic` e `municipio-registado-topic`).
- [x] Ecrã de Perfil Inteligente (Pasta Digital para Feirantes, Dados Oficiais para Autarquias).

### ⏳ A Fazer (Próximos Passos Pós-Férias)
- [x] **Fluxo de Criação de Feiras (Autarquias):** Formulário onde a Câmara define datas, vagas disponíveis e regras do mercado.
- [x] **Fluxo de Candidatura (Feirante):** Vitrine pública de feiras com botão "Candidatar-me" que faz auto-fill dos PDFs guardados na Pasta Digital.
- [x] **Painel de Aprovação (Autarquias):** Backoffice (tabela) para a Câmara ver as candidaturas pendentes, analisar documentos e clicar em "Aprovar" ou "Rejeitar".
- [ ] **Observabilidade e Distributed Tracing:** Adicionar o Micrometer Tracing (antigo Spring Cloud Sleuth) para gerar *Correlation IDs*. Assim, um pedido que entra no Gateway e viaja pelo Kafka pode ser rastreado nos logs antes de empacotar tudo.
- [ ] **Dockerização Final:** Garantir que o projeto levanta 100% de forma limpa apenas com um `docker-compose up` para a apresentação no escritório.

---

## ⭐ 2. O Fator "Uau" (Se houver tempo extra)
*Funcionalidades de alto impacto para brilhar perante os arquitetos, provando resiliência e pensamento a longo prazo.*

- [ ] **Notificações por E-mail Mockadas (Mailhog via Docker):** Adicionar o serviço Mailhog ao `docker-compose.yml` e configurar o Spring Boot (`spring-boot-starter-mail`) para disparar e-mails transacionais automáticos, simulando o ambiente real:
    - **E-mail de Registo Inicial:** Disparado pelo `identidade-service` assim que a conta é submetida.
    - **E-mail de Confirmação e Ativação:** Enviado ao Feirante quando o `mercados-service` confirma o espelhamento do perfil via Kafka.
    - **E-mail de Aprovação de Candidatura:** Disparado automaticamente pelo `mercados-service` quando a Câmara aprova o comerciante na feira ("*Parabéns, tem lugar na feira!*").
- [ ] **Transações Distribuídas com Mock Gateway (Sagas):** O `pagamentos-service` finge processar um pagamento e envia um evento Kafka (`pagamento-concluido`). O `mercados-service` ouve isto e muda a candidatura de `A_AGUARDAR_PAGAMENTO` para `LUGAR_CONFIRMADO`.
- [ ] **Integração de Mapas (Vitrine):** Usar a fórmula de Haversine para listar feiras "Perto de Mim" através de um mapa interativo simples (Leaflet.js).
- [ ] **Dashboard Visual da Autarquia:** Gráfico (ex: Chart.js) no painel do Município mostrando as "Vagas Ocupadas vs Disponíveis".
- [ ] **Storage Persistente com MinIO (Mock AWS S3):** Em vez de guardar PDFs no disco efémero do Docker, ligar a Pasta Digital a um bucket do MinIO, provando que o sistema está pronto para a Cloud.
- [ ] **Resiliência do Gateway (Circuit Breaker):** Adicionar o *Resilience4j* no `identidade-service`. Se o `mercados-service` cair, o Gateway devolve um erro elegante ao invés de ficar bloqueado à espera.
- [ ] **Resiliência do Kafka (Dead Letter Queue):** Criar um tópico de falhas (`error-topic`) e expor um log de auditoria no Frontend para mostrar que o sistema trata as falhas sem perder informação (Poison Pills).
- 
---

## 🚫 3. Fora de Âmbito (O que NÃO vai ter de todo)
*Funcionalidades deliberadamente cortadas para manter o foco arquitetural do PoC e não gastar tempo.*

- ❌ **Integrações de Pagamentos Reais:** Não haverá comunicação direta e real com SIBS, MBWay ou Stripe (substituído pelo Mock).
- ❌ **Chave Móvel Digital / Autenticação.gov:** A burocracia de chaves de testes do Estado consome demasiado tempo. O login por Email/Password nativo é suficiente.
- ❌ **Comunicação com a Autoridade Tributária (AT):** Geração de faturas e SAFT está fora de questão para um PoC por restrições legais e de tempo.
- ❌ **Aplicações Mobile Nativas:** Não haverá código iOS ou Android (Swift/Kotlin). O Angular com Tailwind encarrega-se do comportamento responsivo para os ecrãs móveis (PWA).
- ❌ **Deploy em Cloud Cloud (AWS/Azure):** Configurar Kubernetes clusters e CI/CD pipelines é oneroso. A execução será focada em *containerization* local (Docker).

## 📖 4. Entregáveis de Negócio & Apresentação (Pitch)
*O embrulho final do PoC para garantir que a mensagem passa tanto para perfis técnicos como para a gestão.*

- [ ] **Documentação Técnica Automática (Swagger/OpenAPI):** Adicionar a dependência `springdoc-openapi-starter-webmvc-ui` ao Gateway e aos microsserviços para gerar a documentação viva das APIs automaticamente.
- [ ] **Coleção do Postman:** Exportar uma coleção do Postman com os endpoints organizados (Login, Criar Feira, Candidatura) para que os arquitetos possam testar as APIs por si mesmos.
- [ ] **Diagrama de Arquitetura (C4 Model / Fluxograma):** Um esquema visual (pode ser feito no Draw.io ou Excalidraw) mostrando o Angular a falar com o Gateway, e o Kafka a distribuir mensagens entre as bases de dados isoladas.
- [ ] **Estimativa de Custos de Infraestrutura (FinOps Alto Nível):** Criar uma secção na documentação (ou um slide dedicado) mapeando os custos estimados para passar esta arquitetura de Docker Local para produção na Cloud (AWS ou Azure):
    - *Custos de Computação:* Instâncias para alojar os microsserviços Spring Boot e o Frontend Angular.
    - *Custos de Dados & Mensagens:* Custo estimado de um cluster gerido de Apache Kafka (ex: Confluent Cloud ou AWS MSK) e instâncias de base de dados relacionais (PostgreSQL gerido - AWS RDS).
    - *Custos de Rede:* Tráfego de dados que passa pelo API Gateway.
- [ ] **Apresentação (Slide Deck de 5 a 7 slides):**
    1. **O Problema:** A dor atual das autarquias e feirantes (processos manuais, PDFs repetidos, lentidão).
    2. **A Solução:** Uma plataforma centralizada, mas distribuída na sua arquitetura.
    3. **A Arquitetura (O trunfo técnico):** Falar do Kafka, Eventual Consistency, e segurança centralizada (Gateway).
    4. **Live Demo:** Mostrar o fluxo principal a funcionar (Câmara cria feira -> Feirante candidata-se usando a Pasta Digital).
    5. **Próximos Passos (Roadmap):** Mostrar os Fatores "Uau" e para onde a plataforma pode crescer.