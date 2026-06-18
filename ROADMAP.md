# PoC: Sistema de Gestão de Mercados e Feirantes (Enterprise Edition)

## 🏢 Visão Geral da Arquitetura Real (Estado Atual)
O ecossistema foi consolidado com sucesso numa arquitetura **Orientada a Eventos (EDA)** de alta fidelidade e baseada em microsserviços totalmente isolados (*Bounded Contexts*), comunicando assincronamente através de um barramento central de mensagens.

* **`frontend`**: Aplicação SPA em **Angular** (Standalone & Signals), reativa, com UI de Checkout unificada, Painel Executivo Municipal e Sistema Flutuante de Toasts.
* **`identidade-service` (Gateway)**: Porta de entrada central e barreira de segurança (CORS Preflight, RBAC, JWT com injeção de Headers, roteamento dinâmico de Buckets S3, porta `8080`).
* **`mercados-service`**: Motor core do negócio. Gere Feiras, Candidaturas, Balcão de Inscrições, persistência em disco via MinIO e o Listener assíncrono de transações (porta `8082`).
* **`municipios-service`**: Gestão estendida das Câmaras e Juntas Concelhias (porta `8081`).
* **`cidadaos-service`**: Motor de engajamento do visitante. Governa as interações do utilizador final, avaliações e o histórico de marcadores (porta `8083`).
* **`pagamentos-service` (PRODUÇÃO)**: Microsserviço financeiro completo com base de dados isolada, controlo estrito de idempotência e publicação de vereditos transacionais (porta `8085`).
* **`broker`**: Apache Kafka a garantir *Eventual Consistency* e a orquestração do padrão Saga.
* **`storage`**: Contentor MinIO S3 centralizado com buckets públicos/privados isolados para gestão transparente de assets binários.

---

## 🎯 1. O Core do PoC (Requisitos Mínimos para a Demo)
*O "Caminho Feliz" que prova o valor do produto ao cliente e valida o core transacional.*

### ✅ Concluído (Pronto para Defesa)
- [x] Infraestrutura base (`docker-compose.yml` híbrido e isolamento de redes corrigido).
- [x] Gateway de API e Filtro Global de Autenticação JWT (`JwtGatewayFilter`).
- [x] Criação de Mercados/Feiras públicos com persistência relacional.
- [x] Submissão de Candidaturas por parte dos Feirantes com upload de Portfólio PDF.
- [x] Fluxo de Parecer Técnico do Município (Painel da Câmara para Aprovar/Rejeitar).
- [x] **Balcão de Inscrições Unificado:** Histórico de submissões embutido diretamente no DTO de perfil do Feirante.
- [x] **Event-Driven UI:** Frontend atualiza instantaneamente para o ecrã de sucesso assim que a API financeira emite o aval.
- [x] **Modernização Completa da UX (Substituição de Alerts):** Todos os `alert()` nativos do browser foram eliminados e substituídos por um `ToastService` reativo e flexível no formulário e listagens.
- [x] **Zona de Drag & Drop Premium para Ficheiros:** Área pontilhada interativa para arrastar e largar PDFs (Perfil) e imagens de Cartaz (Camaras) com seletor reativo de abas (Link URL vs Ficheiro Local).

### 🛠️ Em Falta / Próximos Passos (Estabilização do Core)
- [ ] **Observabilidade Local (Micrometer Tracing):** Adicionar dependências do Zipkin/Brave para propagar o `Correlation ID` nos logs do Gateway ao passar pelo Kafka.
- [ ] **Dockerização Completa de Aplicações:** Ativar e validar os perfis de Docker Compose (`profiles: ["apps"]`) para rodar os jars compilados do Java e o build do Angular em contentores isolados.

---

## ⭐ 2. O Fator "Uau" (Arquitetura Avançada e Engenharia de Elite)
*O diferencial técnico que demonstra maturidade arquitetural e garante nota máxima junto dos avaliadores Devoteam.*

### ✅ Concluído (Diferencial Conquistado)
- [x] **Padrão Saga Coreografada:** Comunicação distribuída e assíncrona via Kafka entre o `pagamentos-service` e o `mercados-service` através do tópico `pagamento-concluido`.
- [x] **Database-per-Service Financeiro:** Criação do contentor `postgres-pagamentos` e do volume dedicado, garantindo o isolamento estrito de dados.
- [x] **Blindagem de Idempotência:** Validação na BD local bloqueando cobranças duplicadas em cliques rápidos com um `400 Bad Request`.
- [x] **Interface Multi-Canal Estilo Stripe:** Checkout moderno no Angular com suporte e renderização condicional de abas para **Multibanco**, **Cartão de Crédito** e **Transferência Bancária SEPA**.
- [x] **Desacoplamento de Storage (MinIO Cloud-Native):** Descodificação de fluxos Base64 em bytes no Java e armazenamento físico em buckets S3 (Público/Privado) gerenciados via API Gateway.
- [x] **Dashboard de FinOps Autárquico:** Painel executivo reativo para o `ROLE_MUNICIPO` com métricas consolidadas de faturação prevista por data de feira, vagas e feirantes pendentes.

### 🛠️ Em Falta / Próximos Passos (Mapeado para as Próximas Iterações)
- [ ] **Sistema de Favoritos Reativo (User-Level):** Adicionar um botão de "Estrela / Favorito" nos cartões de feiras do Cidadão autenticado. O clique persiste o vínculo no `cidadaos-service` e dispara um sinal de atualização visual.
- [ ] **Priorização de Ordenação nas Queries (Bubble-Up):** Alterar o endpoint `@GetMapping` de listagem do `mercados-service` para intercetar o e-mail do utilizador e cruzar com os seus favoritos, fazendo com que as feiras favoritadas subam automaticamente para o topo da lista.
- [ ] **Dashboard de FinOps Autárquico:** Painel executivo para o `ROLE_MUNICIPO` com métricas consolidadas de taxas cobradas, taxas pendentes e gráficos de rentabilidade por mercado/evento.
- [ ] **Mapa Interativo do Recinto:** Visualização espacial das bancas do mercado em formato de grelha CSS, permitindo aos feirantes verem a sua banca mudar para "Verde (Ocupado)" reativamente após o sucesso da Saga.
- [ ] **Resiliência com Circuit Breaker:** Adicionar `@CircuitBreaker` do Resilience4j no Gateway para lidar com eventuais quedas súbitas do microsserviço de notificações.
- [ ] **Internacionalização Avançada (i18n):** Configuração do `@angular/localize` para suporte multi-idioma (PT/EN), focando na inclusão de feirantes internacionais e turismo de feiras.
---

## 📖 3. Entregáveis de Negócio & Apresentação (Fase de Produto)
*Preparar a embalagem do projeto para encantar a audiência.*

### 🛠️ Em Falta / Próximos Passos
- [ ] **Carga de Massa de Dados Industrializada (Mega Demo Seed):** Criar a árvore de scripts SQL (`init-scripts/`) e injeções automáticas no MinIO (`minio-init/`) para subir o sistema instantaneamente com 3 Câmaras Municipais (Lisboa, Leiria, Loures), 5 Feirantes fictícios com documentos pré-arquivados e 10 Feiras distribuídas geograficamente com cartazes HD.
- [ ] **Documentação Interativa (Swagger/OpenAPI):** Adicionar os Starters do `springdoc-openapi` nos microsserviços para disponibilizar os ecrãs do Swagger na porta do Gateway.
- [ ] **Diagrama de Sequência da Saga:** Desenhar um fluxograma (Excalidraw/Draw.io) que ilustre a coreografia da mensagem (Angular -> Gateway -> Pagamentos -> Kafka -> Mercados) para suporte visual no Pitch.
- [ ] **Slide Deck Técnico (5 a 7 Slides):** Estruturar a apresentação de 10 minutos focada no problema de negócio (burocracia autárquica) e na solução altamente escalável desenvolvida.